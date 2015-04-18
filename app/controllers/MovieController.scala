package controllers

import play.api.mvc._
import play.api.libs.json.Json
import plex.API
import security.Secured

object MovieController extends Controller with Secured {

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

  def watch(movieId: String, state: String, offset: Double) = withAuth { token => implicit request =>

    println(movieId, state, offset)

    Ok(Json.toJson(
      Json.obj("success" -> true)
    ))
  }

}