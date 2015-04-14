package controllers

import play.api.mvc._
import plex.API

/**
 * Created by tomas on 14-04-15.
 */
object Proxy extends Controller {
  def proxy = Action { implicit request =>
    request.getQueryString("url") match {
      case Some(url) =>
        println("~~~~" + url)
        println(API.defaultAuthenticated(url).asString)
        Ok(API.request(url, _.header("", ""), _.asBytes).body).as("image/jpeg")
      case None =>
        Ok("Error")
    }
  }
}
