import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import plex.Movie

import scala.xml.XML

/**
 * Created by tomas on 14-04-15.
 */
@RunWith(classOf[JUnitRunner])
class MovieSpec extends Specification {

  val correctXmlString = "<Video primaryExtraKey=\"/library/metadata/854\" chapterSource=\"\" updatedAt=\"1428767448\" addedAt=\"1428767431\" originallyAvailableAt=\"2007-11-02\" duration=\"10545940\" art=\"/library/metadata/853/art/1428767448\" thumb=\"/library/metadata/853/thumb/1428767448\" tagline=\"There are two sides to the American dream.\" year=\"2007\" rating=\"7.0\" summary=\"Following the death of his employer and mentor, Bumpy Johnson, Frank Lucas establishes himself as the number one importer of heroin in the Harlem district of Manhattan. He does so by buying heroin directly from the source in South East Asia and he comes up with a unique way of importing the drugs into the United States. Based on a true story.\" contentRating=\"R\" title=\"American Gangster\" type=\"movie\" studio=\"Imagine Entertainment\" key=\"/library/metadata/853\" ratingKey=\"853\">\n<Media has64bitOffsets=\"0\" optimizedForStreaming=\"1\" videoFrameRate=\"24p\" container=\"mp4\" videoCodec=\"h264\" audioCodec=\"aac\" audioChannels=\"2\" aspectRatio=\"1.85\" height=\"1040\" width=\"1920\" bitrate=\"1935\" duration=\"10545940\" id=\"1614\" videoResolution=\"1080\">\n<Part optimizedForStreaming=\"1\" indexes=\"sd\" has64bitOffsets=\"0\" container=\"mp4\" size=\"2550757943\" file=\"/volumes/tomas/downloads/film/American Gangster (2007) [1080p]/American.Gangster.2007.1080p.BluRay.x264.YIFY.mp4\" duration=\"10545940\" key=\"/library/parts/1615/file.mp4\" id=\"1615\"/>\n</Media>\n<Genre tag=\"Crime\"/>\n<Genre tag=\"Drama\"/>\n<Writer tag=\"Steven Zaillian\"/>\n<Director tag=\"Ridley Scott\"/>\n<Country tag=\"USA\"/>\n<Role tag=\"Denzel Washington\"/>\n<Role tag=\"Russell Crowe\"/>\n<Role tag=\"Chiwetel Ejiofor\"/>\n</Video>";

  "Movie" should {
    "render the given XML" in {
      val movie = new Movie(XML.loadString(correctXmlString))
      movie.title must be ("American Gangster")
    }
  }
}
