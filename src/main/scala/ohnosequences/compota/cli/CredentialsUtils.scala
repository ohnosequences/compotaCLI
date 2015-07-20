package ohnosequences.compota.cli

import java.io.File
import com.amazonaws.auth._
import ohnosequences.awstools.ec2.EC2
import com.amazonaws.AmazonClientException
import ohnosequences.logging.ConsoleLogger


object CredentialsUtils {

  val logger = new ConsoleLogger("credentials utils")

  def serializeProvider(provider: AWSCredentialsProvider): String = provider match {

    case pfc: PropertiesFileCredentialsProvider => {
      "PropertiesFileCredentialsProvider"
    }
    case ip: InstanceProfileCredentialsProvider => {
       "InstanceProfileCredentialsProvider"
    }
    case ep: EnvironmentVariableCredentialsProvider => {
      "EnvironmentVariableCredentialsProvider"
    }

    case p => p.toString
  }

  def serializeProviderConstructor(provider: AWSCredentialsProvider): String = provider match {
    case ip: InstanceProfileCredentialsProvider => {
      "new com.amazonaws.auth.InstanceProfileCredentialsProvider()"
    }
    case ep: EnvironmentVariableCredentialsProvider => {
      "new com.amazonaws.auth.EnvironmentVariableCredentialsProvider()"
    }
    case pfc: PropertiesFileCredentialsProvider => {
      val fieldField = pfc.getClass().getDeclaredField("credentialsFilePath")
      fieldField.setAccessible(true)
      val path= fieldField.get(pfc).asInstanceOf[String]
      "new com.amazonaws.auth.PropertiesFileCredentialsProvider(\"\"\"$path$\"\"\")".replace("$path$", path)
    }

    //todo fix!
    case p => ""
  }
  
  def checkProvider(provider: AWSCredentialsProvider): Boolean = {
    try {
      val ec2 = EC2.create(provider)
      val size = ec2.ec2.describeSpotPriceHistory().getSpotPriceHistory.size()
      true
    } catch {      
      case t: AmazonClientException => {
        logger.warn("couldn't receive credentials from (or describeSpotPriceHistory() not allowed)" + serializeProvider(provider))
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
      case Some(f) => {        
        new File(f)
      }
    }

    if (credentialsFile.exists()) {
      val property = new PropertiesFileCredentialsProvider(credentialsFile.getAbsolutePath)
      if(checkProvider(property)) {
        return Some(property)
      }
    } else {
      logger.warn("couldn't find file with credentials: " + credentialsFile.getAbsolutePath)
    }

    val environmentProvider = new EnvironmentVariableCredentialsProvider()
    if (checkProvider(environmentProvider)) {
      return Some(environmentProvider)
    }

    val instanceProfile = new InstanceProfileCredentialsProvider()
    if (checkProvider(instanceProfile)) {
      return Some(instanceProfile)
    }
    
    None
  }
}