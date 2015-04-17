package controllers

import play.api.mvc._
import plex.API
import security.Secured

object Application extends Controller with Secured {

  def index = withAuth { token => implicit request =>
    val movies = API.getMovies(token)
    Ok(views.html.movies(movies, token))
  }

  def movie(movieId: String) = withAuth { token => implicit request =>
    API.getMovie(movieId, token) match {
      case Some(movie) => Ok(views.html.movie(movie, token))
      case None => Ok("wut")
    }
  }
}