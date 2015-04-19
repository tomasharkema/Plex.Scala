import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "should redirect to login page" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get
      status(home) must equalTo(SEE_OTHER)
    }

    "shoud show all the movies" in new WithAppLogin {
      val home = route(LoginUtil.request(GET, "/")).get
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Movies")
    }

    "should show a detail of a movie" in new WithAppLogin {
      val home = route(LoginUtil.request(GET, "/movie/41")).get
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Big Hero 6")
      contentAsString(home) must contain ("<video>")
    }

  }
}
