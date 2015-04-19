
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import org.specs2.execute.AsResult
import org.specs2.execute.Result

/**
 * Created by tomas on 18-04-15.
 */

object LoginUtil extends RouteInvokers with Writeables {

  val loginRequest = FakeRequest(Helpers.POST, "/authenticate")
    .withFormUrlEncodedBody(("username", "testingapp"), ("password", "testingapp1234567890"))

  var _session: Session = _

  var token: String = ""

  def login() {
    _session = session(route(loginRequest).get)
    token = _session.get(Security.username).get
  }

  def request(a:String, b:String) = FakeRequest(a, b).withSession((Security.username, token))

}

abstract class WithAppLogin extends WithApplication {
  override def around[T: AsResult](t: => T): Result = super.around {
    LoginUtil.login()
    t
  }
}