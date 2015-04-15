package controllers

import model.Auth
import play.api.mvc._
import plex.API
import security.Secured

object Application extends Controller with Secured {

  def index = withAuth { token => implicit request =>

    val token = request.cookies.get("token").orNull.value
    val bearer = request.cookies.get("bearer").orNull.value

    val movies = API.getMovies(token)

    Ok(views.html.index(movies, token))
  }

}