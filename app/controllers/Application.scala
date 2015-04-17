package controllers

import play.api.mvc._
import plex.API
import security.Secured

object Application extends Controller with Secured {

  def index = withAuth { token => implicit request =>

    println(token)

    val movies = API.getMovies(token)
    Ok(views.html.index(movies, token))
  }
}