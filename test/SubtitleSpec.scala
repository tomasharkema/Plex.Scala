import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import scala.io.Source

/**
 * Created by tomas on 19-04-15.
 */
@RunWith(classOf[JUnitRunner])
class SubtitleSpec extends Specification {

  def testSrt = Source.fromURL(getClass.getResource("/testSubtitle.srt"))
  def testVtt = Source.fromURL(getClass.getResource("/testSubtitle.vtt"))

  "SubtitleUtils" should {
    "check if resource 'testSubtitle.srt' is available" in {
      testSrt.getLines() must have size 7529
    }

    "convert SRT to VTT" in {
      val converter = utils.SubtitlesUtils.convertSRTToVVT(testSrt.mkString)
      converter must startWith("WEBVTT")

      var vtt = testVtt.mkString

      converter must have size vtt.length
    }

  }
}