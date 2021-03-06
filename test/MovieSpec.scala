import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import play.mvc.Security
import plex.Movie

import scala.xml.XML

/**
 * Created by tomas on 14-04-15.
 */

import com.netaporter.uri.dsl._

@RunWith(classOf[JUnitRunner])
class MovieSpec extends Specification {

  val correctXmlString = "<Video primaryExtraKey=\"/library/metadata/854\" chapterSource=\"\" updatedAt=\"1428767448\" addedAt=\"1428767431\" originallyAvailableAt=\"2007-11-02\" duration=\"10545940\" art=\"/library/metadata/853/art/1428767448\" thumb=\"/library/metadata/853/thumb/1428767448\" tagline=\"There are two sides to the American dream.\" year=\"2007\" rating=\"7.0\" summary=\"Following the death of his employer and mentor, Bumpy Johnson, Frank Lucas establishes himself as the number one importer of heroin in the Harlem district of Manhattan. He does so by buying heroin directly from the source in South East Asia and he comes up with a unique way of importing the drugs into the United States. Based on a true story.\" contentRating=\"R\" title=\"American Gangster\" type=\"movie\" studio=\"Imagine Entertainment\" key=\"/library/metadata/853\" ratingKey=\"853\">\n<Media has64bitOffsets=\"0\" optimizedForStreaming=\"1\" videoFrameRate=\"24p\" container=\"mp4\" videoCodec=\"h264\" audioCodec=\"aac\" audioChannels=\"2\" aspectRatio=\"1.85\" height=\"1040\" width=\"1920\" bitrate=\"1935\" duration=\"10545940\" id=\"1614\" videoResolution=\"1080\">\n<Part optimizedForStreaming=\"1\" indexes=\"sd\" has64bitOffsets=\"0\" container=\"mp4\" size=\"2550757943\" file=\"/volumes/tomas/downloads/film/American Gangster (2007) [1080p]/American.Gangster.2007.1080p.BluRay.x264.YIFY.mp4\" duration=\"10545940\" key=\"/library/parts/1615/file.mp4\" id=\"1615\"/>\n</Media>\n<Genre tag=\"Crime\"/>\n<Genre tag=\"Drama\"/>\n<Writer tag=\"Steven Zaillian\"/>\n<Director tag=\"Ridley Scott\"/>\n<Country tag=\"USA\"/>\n<Role tag=\"Denzel Washington\"/>\n<Role tag=\"Russell Crowe\"/>\n<Role tag=\"Chiwetel Ejiofor\"/>\n</Video>"

  "Movie" should {
    "render the given XML" in {
      val movie = Movie.parseNode(XML.loadString(correctXmlString))
      movie.title must startWith("American Gangster")
      movie.key must startWith("853")
    }
  }

  "MovieWatch" should {
    "should give success" in new WithAppLogin {
      val watchUpdate = route(FakeRequest(POST, "/watch" & ("movieId" -> "44") & ("state" -> "playing") & ("offset" -> 42.7)).withSession(("username", LoginUtil.token))).get

      status(watchUpdate) must equalTo(OK)
      contentType(watchUpdate) must beSome.which(_ == "application/json")
    }

    "should have updated videoState" in new WithApplication {
      val watchUpdate = route(FakeRequest(POST, "/watch" & ("movieId" -> "44") & ("state" -> "playing") & ("offset" -> 42.7) & ("token" -> ""))).get
    }

  }
}
