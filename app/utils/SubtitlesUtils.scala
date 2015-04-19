package utils

/**
 * Created by tomas on 19-04-15.
 */

object SubtitlesUtils {
  private val pattern = "\\s+\\s+(.+)".r

  private def parseString(srt: String) = srt.split("\r\n\r\n").map{ frag =>
    frag.split("\r\n")
  }

  def convertSRTToVVT(file: String): String = {

    val res = parseString(file)
      .map { frag =>
        println(frag)
        val f = frag.splitAt(1)._2
        // replace , with .
        f(0) = f(0).replace(",", ".")

        f.mkString("\r\n")
      }
      .mkString("WEBVTT\r\n", "\r\n\r\n", "")

    res
  }

}
