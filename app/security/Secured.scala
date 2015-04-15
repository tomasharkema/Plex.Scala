package security

import model.Auth
import play._
import play.api._
import play.api.mvc._
import play.core.Router
import plex.API

/**
 * Created by tomas on 15-04-15.
 */
trait Secured {
  self: Controller =>

  def token(request: RequestHeader) = request.session.get(Security.username)
  def bearer(request: RequestHeader) = request.cookies.get("bearer").orNull.value

  def onUnauthorized(request: RequestHeader) = Results.Redirect(controllers.routes.Login.login())

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(token, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  /**
   * This method shows how you could wrap the withAuth method to also fetch your user
   * You will need to implement UserDAO.findOneByUsername
   */
  def withUser(f: Auth => Request[AnyContent] => Result) = withAuth { bearer => implicit request =>
    API.authentication(bearer).map { token =>
      f(Auth(token, bearer))(request)
    }.getOrElse(onUnauthorized(request))
  }

}
