package controllers

import play.api.mvc._
import plex.API
import security.Secured

object Proxy extends Controller with Secured {
  def proxy = withAuth { token => implicit request =>
    request.getQueryString("url") match {
      case Some(url) =>
        println("~~~~" + url)

        routes.Login.login()

        println(API.defaultAuthenticated(url, token).asString)
        Ok(API.request(url, token, _.header("", ""), _.asBytes).body).as("image/jpeg")
      case None =>
        NotFound
    }
  }
}
