package ohnosequences.compota.cli

import java.nio.file.Files

import ohnosequences.logging.ConsoleLogger
import org.eclipse.jgit.api.Git
import java.io.{IOException, FileInputStream, File}

import ohnosequences.awstools.ec2.{Tag, EC2}
import com.amazonaws.auth.{AWSCredentialsProvider, InstanceProfileCredentialsProvider, PropertiesCredentials}
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest
import scala.collection.JavaConversions._
import java.util.Properties
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.AmazonClientException
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient

import scala.util.Try


object CompotaCLI {
  val s3pattern = """[a-zA-Z0-9][a-zA-Z0-9-\.]*"""
  val keyNamePattern = """[a-zA-Z0-9][a-zA-Z0-9-]*"""


  def help(): Unit = {
    logger.info("compotaCLI valid commands:")
    logger.info("")
    logger.info("configure bucket [credentialsFile]")
    logger.info("create repository[:tag] [credentialsFile]")
    logger.info("configure [credentialsFile]")
  }

  def main(rawArgs: Array[String]) {
    val argsList = rawArgs.toList
    argsList match {
      case "configure" :: "bucket" :: args => {
        CredentialsUtils.retrieveCredentialsProvider(args.headOption) match {
          case Some(provider) => {
            bucketSetup(EC2.create(provider))
          }
          case None => {
            logger.error("couldn't retrieve credentials provider")
          }
        }
      }

      case "create" :: repo :: args =>  {
        val repoTag = parseRepo(repo)
        createNispero(CredentialsUtils.retrieveCredentialsProvider(args.headOption), repoTag._2, createUrl(repoTag._1))
      }

      case "configure" :: args =>  {
        CredentialsUtils.retrieveCredentialsProvider(args.headOption) match {
          case Some(provider) => {
            val iam = new AmazonIdentityManagementClient(provider)
            accountSetup(EC2.create(provider), iam)
          }
          case None => {
            logger.error("couldn't retrieve credentials provider")
          }
        }
      }

      case "help" :: Nil => help()

      case _ => {
        logger.error("wrong command: " + argsList)
        help()
      }
    }
  }


  val logger = new ConsoleLogger("compotaCLI")

  def createSecurityGroup(ec2: EC2, securityGroup: String, port: Int): Option[String] = {
    logger.info("creating security group: " + securityGroup)
    ec2.createSecurityGroup(securityGroup)

    val sg = Utils.waitForResource(ec2.ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest()
      .withGroupNames(securityGroup)
    ).getSecurityGroups.headOption)


    if(sg.isEmpty) {
      logger.error("couldn't create security group: " + securityGroup)
      System.exit(1)
    }

    //enable ssh connection
    logger.info("enabling ports")
    ec2.enablePortForGroup(securityGroup, 22)
    ec2.enablePortForGroup(securityGroup, 80)
    ec2.enablePortForGroup(securityGroup, port)

    sg.map(_.getGroupId)

  }


  val bucketsSuffixTag = "compota"
  val securityGroup = "compota"


  def getConfiguredBucketSuffix(ec2: EC2, securityGroup: String): Option[String] = {

    ec2.ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest()
      .withGroupNames(securityGroup)
    ).getSecurityGroups.head.getTags.find(_.getKey.equals(bucketsSuffixTag)).flatMap { tag =>
      val value = tag.getValue
     // println("value: " + value)
      if(value.matches(s3pattern)) {
        Some(value)
      } else {
        None
      }
    }
  }

  def setBucketSuffix(ec2: EC2, securityGroup: String, bucketSuffix: String)= Try {
    ec2.createTags(securityGroup, List(Tag(bucketsSuffixTag, bucketSuffix)))
  }

  def bucketSetup(ec2: EC2) = {
    val bucketsSuffixValue = getConfiguredBucketSuffix(ec2, securityGroup)
    print("type suffix for artifacts buckets")
    bucketsSuffixValue match {
      case Some(name) => println(" [" + name + "]:")
      case None => println(":")
    }

    var takenBucketSuffix = ""

    while (takenBucketSuffix.isEmpty) {
      val newBucketsSuffixValue: String = io.StdIn.readLine()
      takenBucketSuffix = (newBucketsSuffixValue, bucketsSuffixValue) match {
        case ("", Some(suffix)) => suffix
        case (suffix, _) if suffix.matches(s3pattern) => {
          setBucketSuffix(ec2, securityGroup, suffix)
          suffix
        }
        case _ => logger.warn("bucket suffix should have format: " + s3pattern); ""
      }
    }

  }

  def accountSetup(ec2: EC2, iam: AmazonIdentityManagementClient) = {

    val iamRole = "compota"
    val iamRoleLegacy = "nispero"
    val keyName = "compota"

    val port = 443 //https
    val id = createSecurityGroup(ec2, securityGroup, port)

    bucketSetup(ec2)

    logger.info("creating IAM role: " + iamRole)
    if(!RoleCreator.roleExists(iamRole, iam)){
      logger.info("creating IAM role: " + iamRole)
      RoleCreator.createGodRole(iamRole, iam)
    }
    
    logger.info("creating legacy IAM role: " + iamRoleLegacy)
    if(!RoleCreator.roleExists(iamRoleLegacy, iam)){
      logger.info("creating IAM role: " + iamRoleLegacy)
      RoleCreator.createGodRole(iamRoleLegacy, iam)
    }

    logger.info("account configured")
  }



  def createNispero(credentialsProvider: Option[AWSCredentialsProvider], tag: Option[String], url: String) {

    val resolverKeys: Map[String, String] = credentialsProvider.map { provider =>
      Map("credentialsProvider" -> CredentialsUtils.serializeProviderConstructor(provider))
    }.getOrElse(Map())

    val bucketSuffixMapping: Map[String, String] = credentialsProvider.flatMap { provider =>
      getConfiguredBucketSuffix(EC2.create(provider), securityGroup)
    }.map { suffix =>
      Map(bucketsSuffixTag -> suffix)
    }.getOrElse(Map())

    val predef = bucketSuffixMapping ++ resolverKeys ++ Map(
      "password" -> java.lang.Long.toHexString((Math.random() * 100000000000L).toLong
      )
    )

    fetch(tag, url, predef, defaultPrinter)

  }


  def createUrl(repo: String) = "https://github.com/" +  repo + ".git"

  def parseRepo(repoTag: String): (String, Option[String]) = {
    repoTag.split("/").toList match {
      case org :: repo :: tag :: Nil => (org + "/" + repo, Some(tag))
      case org :: repo :: Nil => (org + "/" + repo, None)
      case _ => throw new Error("wrong repository")
    }
  }




  def fixName(name: String): String = {
    val res = new StringBuilder()
    name.split("[^a-zA-Z0-9]+").foreach { s =>
      if(!res.isEmpty) {
        res.append("_")
      }
      res.append(s.toLowerCase)
    }
    var fixedName = res.toString()
    if(!fixedName.matches("[a-z].+")) {
      fixedName = "the" + fixedName
    }
    if(!res.toString().equals(name)) {
      logger.warn("name converted to " + fixedName)
    }
    fixedName
  }

  def defaultPrinter(key: String, value: String) = key + " [" + value + "]: "

  def createMapping(defaultProps: File,
                    predef: Map[String, String],
                    propertyPrinter: (String, String) => String = defaultPrinter
                     ): Map[String, String] = {

    var result = Map[String, String]()
    val properties: Properties = new Properties()
    properties.load(new FileInputStream(defaultProps))

    logger.info("please specify the settings:")

    properties.foreach { case (name, value) =>

      val fixedValue = predef.get(name) match {
        case None => value
        case Some(predefValue) => predefValue
      }

      // var actualValue
      //logger.info("print here")
      println(propertyPrinter(name, fixedValue))
      val actualValue = readLine() match {
        case "" => fixedValue
        case v => v
      }


      result += ("$" + name + "$" -> actualValue)
    }
    result
  }

  def fetch(tag: Option[String], url: String, predef: Map[String, String], propertyPrinter: (String, String) => String) {

    val dst = Utils.createTempDir()

    logger.info("cloning template from repository")

    clone(url, tag, dst)

    val props = new File(dst, "src/main/g8/default.properties")
    val preMapping = createMapping(props, predef, propertyPrinter)

    val mapping = preMapping + ("$name$" -> fixName(preMapping("$name$")))



    //remove properties file

    Try(props.delete())

    val dst2 = new File(mapping("$name$"))


    val t = new File(dst, "src/main/g8")
    val files = Utils.recursiveListFiles(t)


    Try(Files.delete(dst2.toPath))
    dst.mkdir()

    val mapIgnore = {file: File =>
      !(file.getName.endsWith(".scala") || file.getName.endsWith(".sbt"))
    }

    Utils.copyAndReplace(files, dst2, mapping, t.getPath, mapIgnore)


    try {
      Files.delete(dst.toPath)
    } catch {
      case e: IOException => logger.warn("unable to delete: " + dst.getPath)
    }

    logger.info("template applied to " + dst2.getAbsolutePath)

  }

  def clone(url: String, tag: Option[String], dst: File) {
    try {
      Files.delete(dst.toPath)
    } catch {
      case e: IOException => logger.warn("unable to delete: " + dst.getPath)
    }

    val cmd = Git.cloneRepository()
      .setDirectory(dst)
      .setURI(url)
    val repo = cmd.call()

    tag match {
      case None => ()
      case Some(t) => {
        repo.checkout()
          .setName("tags/" + t)
          .call()
      }
    }
  }

}