import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import java.io.{FileReader, InputStreamReader, Reader}

object MainClass {
  def getEngine:ScriptEngine = {
    val manager = new ScriptEngineManager()
    manager.getEngineByName("jruby")
  }

  def main(args:Array[String]):Unit = {
    val classLoader = getClass.getClassLoader
    val rdr = new InputStreamReader(classLoader.getResourceAsStream("ruby/read_cloudtrail.rb"))

    val engine = getEngine
    try {
      engine.eval(rdr)
    } catch {
      case e:ScriptException=>
        println(s"Could not run embedded script: $e")
        sys.exit(1)
    }
  }
}