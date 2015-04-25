package security

import model.{User, Auth}
import play._
import play.api._
import play.api.mvc._
import play.core.Router
import plex.API

import scala.concurrent.Future

/**
 * Created by tomas on 15-04-15.
 */
trait Secured {
  self: Controller =>

  def token(request: RequestHeader) = request.session.get(Security.username)
  def bearer(request: RequestHeader) = request.cookies.get("bearer").orNull.value

  def onUnauthorized(request: RequestHeader) = Results.Redirect(controllers.routes.Login.login())
  def onUnauthorizedFuture(request: RequestHeader) = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Future.apply(Results.Redirect(controllers.routes.Login.login()))
  }

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(token, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }

  def withAuthFuture(f: => String => Request[AnyContent] => Future[Result]) = {
    Security.Authenticated(token, onUnauthorized) { user =>
      Action.async(request => f(user)(request))
    }
  }

  /**
   * This method shows how you could wrap the withAuth method to also fetch your user
   * You will need to implement UserDAO.findOneByUsername
   */

//  def withUserFuture(f: (User, String) => Request[AnyContent] => Future[Result]) = withAuthFuture { token => implicit request =>
//    import scala.concurrent.ExecutionContext.Implicits.global
//    API.getUser(token).map { u =>
//      u.map { user =>
//        //f(user, token)(request)
//        Action.async(request => f(user)(request))
//      }.getOrElse(onUnauthorized(request))
//    }
//  }

  def withUserFuture(f: (User, String) => Request[AnyContent] => Future[Result]) = withAuthFuture { token => implicit request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    API.getUser(token).map {
      case Some(user) => f(user, token)(request)
      case None =>
        import scala.concurrent.ExecutionContext.Implicits.global
        Future.apply(onUnauthorized(request))
    }.mapTo
  }
}
