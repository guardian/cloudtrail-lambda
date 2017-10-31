import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import java.io.{FileReader, InputStreamReader, Reader}
import java.util

import com.amazonaws.services.lambda.runtime._

import scala.collection.JavaConverters._
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.event.S3EventNotification
import com.amazonaws.services.s3.event.S3EventNotification.{S3BucketEntity, S3Entity, S3EventNotificationRecord, S3ObjectEntity}

class FakeLogger extends LambdaLogger {
  override def log(string: String): Unit = {}
}

class FakeClient extends Client {
  override def getAppPackageName: String = ""

  override def getInstallationId: String = ""

  override def getAppTitle: String = ""

  override def getAppVersionCode: String = ""

  override def getAppVersionName: String = ""
}

class FakeClientContext extends ClientContext {
  override def getCustom: util.Map[String, String] = Map("test"->"test").asJava

  override def getEnvironment: util.Map[String, String] = Map("test"->"test").asJava

  override def getClient: Client = new FakeClient
}

class FakeContext extends Context {
  override def getFunctionName: String = "testFunction"

  override def getRemainingTimeInMillis: Int = 10000

  override def getLogger: LambdaLogger = new FakeLogger

  override def getFunctionVersion: String = "1.0"

  override def getMemoryLimitInMB: Int = 512

  override def getClientContext: ClientContext = new FakeClientContext

  override def getLogStreamName: String = "logStream"

  override def getInvokedFunctionArn: String = "arn:aws:lambda:eu-west-1:name"

  override def getIdentity: CognitoIdentity = null

  override def getLogGroupName: String = "test logs"

  override def getAwsRequestId: String = "74B9CB83-FE41-4039-9142-BD5C1D27FC64"
}

object MainClass {

  def main(args:Array[String]):Unit = {
    val eventList = Seq(
      new S3EventNotificationRecord("no-region",
        "created","s3","2017-01-01T00:00:00Z","1",null,null,
        new S3Entity("configname",new S3BucketEntity("bucket-name",null,"arn"),new S3ObjectEntity("filename",123456L,"eTag","1"),"schemaversion"),
        null)
    )
    val fakeEvent = new S3Event(eventList.asJava)
    val l = new CloudtrailLambda

    l.handleRequest(fakeEvent,new FakeContext)
  }
}