import java.io.InputStreamReader
import javax.script._

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.event.S3EventNotification
import java.util


import scala.collection.JavaConverters._

class CloudtrailLambda extends RequestHandler[S3Event, Unit] {
  def getEngine:ScriptEngine = {
    val manager = new ScriptEngineManager()
    manager.getEngineByName("jruby")
  }

  /*
  maps an S3EventNotification record into a simple String->String map to send to Ruby
   */
  def extractS3Info(record:S3EventNotification.S3EventNotificationRecord) = {
    Map(
      "bucket" -> record.getS3.getBucket.getName,
      "key" -> record.getS3.getObject.getKey,
      "versionId" -> record.getS3.getObject.getVersionId,
      "size" -> record.getS3.getObject.getSizeAsLong.toString,
      "region" -> record.getAwsRegion,
      "eventName" -> record.getEventName,
      "eventSource" -> record.getEventSource,
      "eventTime" -> record.getEventTime.toString()
    ).asJava
  }

  /*
  binds the global variables in the called script
   */
  def setupBindings(records:Seq[S3EventNotification.S3EventNotificationRecord]):Bindings = {
    val bindings = new SimpleBindings()
    bindings.put("records",records.map(extractS3Info).asJava)
    bindings
  }

  /*
  main lambda event handler
   */
  override def handleRequest(incomingEvent: S3Event, context: Context): Unit = {
    val classLoader = getClass.getClassLoader
    val rdr = new InputStreamReader(classLoader.getResourceAsStream("ruby/read_cloudtrail.rb"))

    val engine = getEngine
    val records = incomingEvent.getRecords
    try {
      val context = engine.getContext
      //magic number refers to SCOPE_GLOBAL - can't yet find a constant definition to use here
      context.setBindings(setupBindings(records.asScala),200)
      engine.eval(rdr)
    } catch {
      case e:ScriptException=>
        println(s"Could not run embedded script: $e")
        sys.exit(1)
    }
  }
}