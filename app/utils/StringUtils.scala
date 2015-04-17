package utils

/**
 * Created by tomas on 18-04-15.
 */
object StringUtils {
  implicit class StringImprovements(val s: String) {
    import scala.util.control.Exception._
    def toIntOpt = catching(classOf[NumberFormatException]) opt s.toInt
  }
}