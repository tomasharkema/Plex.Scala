package controllers

import play.api._
import play.api.mvc._
import plex.API

object Application extends Controller {

  def index = Action {

    val movies = API.getMovies.map(m => m.movieName).reduce(_ + ",\n" + _)

    Ok(views.html.index(movies))
  }

}