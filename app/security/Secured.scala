package security

import java.lang.Exception

import akka.actor.Status._
import akka.dispatch.sysmsg.Failed
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

  def withUserFuture(f: (User, String) => Request[AnyContent] => Future[Result]) = withAuthFuture { token => implicit request =>
    import scala.concurrent.ExecutionContext.Implicits.global
    val getUser = API.getUser(token).map {
      case Some(user) => f(user, token)(request)
      case None => Future.apply(onUnauthorized(request))
    }
    getUser.flatMap { s =>
      s
    }
  }
}
