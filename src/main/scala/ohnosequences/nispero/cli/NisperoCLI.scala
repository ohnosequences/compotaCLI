package ohnosequences.nispero.cli

import org.eclipse.jgit.api.Git
import java.io.{IOException, FileInputStream, File}

import ohnosequences.awstools.ec2.{Tag, EC2}
import com.amazonaws.auth.{AWSCredentialsProvider, InstanceProfileCredentialsProvider, PropertiesCredentials}
import org.clapper.avsl.Logger
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest
import scala.collection.JavaConversions._
import java.util.Properties
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.AmazonClientException
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient
import ohnosequences.nispero.credentials.CredentialsUtils
import org.apache.commons.io.FileUtils


case class Exit(code: Int) extends xsbti.Exit

class NisperoCLI extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = {
    NisperoCLI.main(config.arguments)
    Exit(0)
  }
}

object NisperoCLI {
  val s3pattern = """[a-zA-Z0-9][a-zA-Z0-9-\.]*"""
  val keyNamePattern = """[a-zA-Z0-9][a-zA-Z0-9-]*"""

  val logger = Logger(this.getClass)

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
    ec2.enablePortForGroup(securityGroup, port)

    sg.map(_.getGroupId)

  }



  val sbtCommand = if (System.getProperty("os.name").toLowerCase.contains("win")) {
    "sbt.bat"
  } else {
    "sbt"
  }


  val bucketsSuffixTag = "bucketSuffix"
  val securityGroup = "nispero"

  def getConfiguredBucketSuffix(ec2: EC2, securityGroup: String): Option[String] = {

    ec2.ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest()
      .withGroupNames(securityGroup)
    ).getSecurityGroups.head.getTags.find(_.getKey.equals(bucketsSuffixTag)).flatMap { tag =>
      val value = tag.getValue
      println("value: " + value)
      if(value.matches(s3pattern)) {
        Some(value)
      } else {
        None
      }
    }
  }

  def accountSetup(ec2: EC2, iam: AmazonIdentityManagementClient) = {

    val iamRole = "nispero"
    val keyName = "nispero"

    val port = 443 //https


    val id = createSecurityGroup(ec2, securityGroup, port)

    val bucketsSuffixValue = getConfiguredBucketSuffix(ec2, securityGroup)

    print("type suffix for artifacts buckets")
    bucketsSuffixValue match {
      case Some(name) => println(" [" + name + "]:")
      case None => println(":")
    }

    var takenBucketSuffix = ""

    while (takenBucketSuffix.isEmpty) {

      val newBucketsSuffixValue = readLine()

      takenBucketSuffix = (newBucketsSuffixValue, bucketsSuffixValue) match {
        case ("", Some(bucket)) => bucket
        case (bucket, _) if bucket.matches(s3pattern) => bucket
        case _ => logger.warn("bucket suffix should have format: " + s3pattern); ""
      }
    }

    ec2.createTags(id.get, List(Tag(bucketsSuffixTag, takenBucketSuffix)))


    println("type key pair name (type ENTER if it is not needed):")

    var keyPairName = readLine()

    if (!keyPairName.isEmpty) {
      while(!keyPairName.matches(keyNamePattern)) {
        logger.warn("key pair name should have format: " + keyNamePattern)
        keyPairName = readLine()
      }

      logger.info("creating key pair: " + keyPairName)
      ec2.createKeyPair(keyPairName, Some(new File(keyPairName + ".pem")))
    }

    logger.info("creating IAM role: " + iamRole)
    if(!RoleCreator.roleExists(iamRole, iam)){
      logger.info("creating IAM role: " + iamRole)
      RoleCreator.createGodRole(iamRole, iam)
    }

    logger.info("acount configured")
  }



  def createNispero(credentialsProvider: AWSCredentialsProvider, tag: Option[String], url: String) {

    val ec2 = EC2.create(credentialsProvider)

    val resolverKeys = Map(
      "resolver-credentials-provider" -> CredentialsUtils.generateCall(credentialsProvider)
    )

   // val iam = new AmazonIdentityManagementClient(credentialsProvider)

    val bucketSuffixMapping = getConfiguredBucketSuffix(ec2, securityGroup) match {
      case None => Map[String, String]()
      case Some(bb) => Map(bucketsSuffixTag -> bb)
    }

    println(bucketSuffixMapping)

    val predef = bucketSuffixMapping ++ resolverKeys ++ Map(
      "password" -> java.lang.Long.toHexString((Math.random() * 100000000000L).toLong)
    )
    fetch(tag, url, predef, defaultPrinter)

  }

  def retrieveCredentialsProvider(file: Option[String]): AWSCredentialsProvider = {
    CredentialsUtils.retrieveCredentialsProvider(file) match {
      case None => {
        throw new Error("couldn't retrieve credentials provider")
      }
      case Some(provider) => provider
    }
  }


  def createUrl(repo: String) = "https://github.com/" +  repo + ".git"

  def parseRepo(repoTag: String): (String, Option[String]) = {
    repoTag.split("/").toList match {
      case org :: repo :: tag :: Nil => (org + "/" + repo, Some(tag))
      case org :: repo :: Nil => (org + "/" + repo, None)
      case _ => throw new Error("wrong repository")
    }
  }


  def main(args: Array[String]) {
    val argsList = args.toList
    argsList match {
      case "create" :: repo :: Nil =>  {
        val provider = retrieveCredentialsProvider(None)
        val repoTag = parseRepo(repo)
        createNispero(provider, repoTag._2, createUrl(repoTag._1))
      }

      case "create" :: repo :: file :: Nil =>  {
        val provider = retrieveCredentialsProvider(Some(file))
        val repoTag = parseRepo(repo)
        createNispero(provider, repoTag._2, createUrl(repoTag._1))
      }

      case "configure" :: Nil =>  {
        val provider = retrieveCredentialsProvider(None)
        val iam = new AmazonIdentityManagementClient(provider)
        accountSetup(EC2.create(provider), iam)
      }

      case "configure" :: file :: Nil =>  {
        val provider = retrieveCredentialsProvider(Some(file))
        val iam = new AmazonIdentityManagementClient(provider)
        accountSetup(EC2.create(provider), iam)
      }
      case _ => logger.error("wrong command")
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
    props.delete()

    val dst2 = new File(mapping("$name$"))


    val t = new File(dst, "src/main/g8")
    val files = Utils.recursiveListFiles(t)


    FileUtils.deleteDirectory(dst2)
    dst.mkdir()

    val mapIgnore = {file: File =>
      !(file.getName.endsWith(".scala") || file.getName.endsWith(".sbt"))
    }

    Utils.copyAndReplace(files, dst2, mapping, t.getPath, mapIgnore)


    try {
      FileUtils.deleteDirectory(dst)
    } catch {
      case e: IOException => logger.warn("unable to delete: " + dst.getPath)
    }

    logger.info("template applied to " + dst2.getAbsolutePath)

  }

  def clone(url: String, tag: Option[String], dst: File) {
    try {
      FileUtils.deleteDirectory(dst)
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