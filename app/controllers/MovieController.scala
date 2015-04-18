package controllers

import play.api.mvc._
import play.api.libs.json._
import play.modules.reactivemongo._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.collection.JSONCollection
import plex.{MovieState, API}
import security.Secured
import scala.concurrent.Future

object MovieController extends Controller with Secured with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("movies")

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

  def watch(movieId: String, state: String, offset: Double, token: String) = withAuth { token => implicit request =>

    println(movieId)

    val futureUpdate = collection.insert(
      Json.obj("movieId" -> movieId,
        "user" -> token,
        "offset" -> JsNumber(offset))
    )

    Ok(Json.toJson(
      Json.obj("success" -> true)
    ))
  }
}