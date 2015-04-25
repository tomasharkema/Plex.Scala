package controllers

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import model.UserForm
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import plex.API
import views.html
import Action.async

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by tomas on 15-04-15.
 */
object Login extends Controller {

  val loginForm = Form(
    mapping(
      "username" -> text,
      "password" -> text
    ) (UserForm.apply) (UserForm.unapply)
  )

  def check(user: UserForm): Future[Option[String]] = {
    def bearer = BaseEncoding.base64().encode((user.username + ":" + user.password).getBytes(Charsets.UTF_8))
    API.authentication(bearer)
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.apply(BadRequest(html.login(formWithErrors))),
      user => {
        check(user).map {
          case Some(token) => Redirect(routes.MovieController.index).withSession(Security.username -> token)
          case None => BadRequest(html.login(loginForm.fill(user)))
        }
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.Login.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }

}
