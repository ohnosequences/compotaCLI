package ohnosequences.compota.cli

import java.io.File
import com.amazonaws.auth._
import ohnosequences.awstools.ec2.EC2
import com.amazonaws.AmazonClientException
import ohnosequences.logging.ConsoleLogger

case class FileCredentialsProvider(file: File) extends AWSCredentialsProvider {
  var staticProvider = new PropertiesCredentials(file)

  def getCredentials: AWSCredentials = staticProvider

  def refresh(): Unit = {staticProvider = new PropertiesCredentials(file)}
}

object CredentialsUtils {

  val logger = new ConsoleLogger("credentials utils")

  def print(provider: AWSCredentialsProvider) = provider match {
    case ip: InstanceProfileCredentialsProvider => {
       "InstanceProfileCredentialsProvider"
    }
    case ep: EnvironmentVariableCredentialsProvider => {
      "EnvironmentVariableCredentialsProvider"
    }
    case pp: FileCredentialsProvider => {
      pp.toString
    }
      
    case p => p.toString
  }

  def generateCall(provider: AWSCredentialsProvider) = provider match {
    case ip: InstanceProfileCredentialsProvider => {
      "new com.amazonaws.auth.InstanceProfileCredentialsProvider()"
    }
    case ep: EnvironmentVariableCredentialsProvider => {
      "new com.amazonaws.auth.EnvironmentVariableCredentialsProvider()"
    }
    case pp: FileCredentialsProvider => {
      "new com.amazonaws.auth.PropertiesFileCredentialsProvider(\"\"\"$file$\"\"\")".replace("$file$", pp.file.getAbsolutePath)
    }

    //todo fix!
    case p => ""
  }
  
  def check(provider: AWSCredentialsProvider): Boolean = {
    try {
      val ec2 = EC2.create(provider)
      val size = ec2.ec2.describeSpotPriceHistory().getSpotPriceHistory.size()
      true
    } catch {      
      case t: AmazonClientException => {
        logger.warn("couldn't receive credentials from (or describeSpotPriceHistory() not allowed)" + print(provider))
        false
      }
    }
  }


  def retrieveCredentialsProvider(file: Option[String]): Option[AWSCredentialsProvider] = {
    
    val credentialsFile = file match {
      case None => {
        val defaultLocation = System.getProperty("user.home")
        new File(defaultLocation, "compota.credentials")
      }
      case Some(file) => {        
        new File(file)
      }
    }

    if(credentialsFile.exists()) {
      val property = new FileCredentialsProvider(credentialsFile)
      if(check(property)) {
        return Some(property)
      }
    } else {
      logger.warn("couldn't find file with credentials: " + credentialsFile.getAbsolutePath)
    }

    val instanceProfile = new InstanceProfileCredentialsProvider()
    if(check(instanceProfile)) {
      return Some(instanceProfile)
    }

    val environmentProvider = new EnvironmentVariableCredentialsProvider()
    if(check(environmentProvider)) {
      return Some(environmentProvider)
    }
    None
  }
}