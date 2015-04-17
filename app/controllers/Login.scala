package controllers

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import model.User
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import plex.API
import views.html

/**
 * Created by tomas on 15-04-15.
 */
object Login extends Controller {

  val loginForm = Form(
    mapping(
      "username" -> text,
      "password" -> text
    ) (User.apply) (User.unapply)
  )

  def check(user: User): Option[String] = {
    def bearer = BaseEncoding.base64().encode((user.username + ":" + user.password).getBytes(Charsets.UTF_8))
    API.authentication(bearer)
  }

  def login = Action {
    Ok(views.html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => {
        check(user) match {
          case Some(token) => Redirect(routes.Application.index).withSession(Security.username -> token)
          case None => BadRequest(html.login(loginForm.fill(user)))
        }
      }
    )
  }

  def logout = Action {
    Redirect(routes.Login.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }

}
