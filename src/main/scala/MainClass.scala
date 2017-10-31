import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import java.io.{FileReader, Reader}

object MainClass {
  def getEngine:ScriptEngine = {
    val manager = new ScriptEngineManager()
    manager.getEngineByName("jruby")
  }

  def main(args:Array[String]):Unit = {
    val engine = getEngine
    engine.eval("puts 'Hello world!'")

  }
}