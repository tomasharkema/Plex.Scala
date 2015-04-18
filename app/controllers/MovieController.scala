package controllers

import java.util.Date

import play.api.mvc._
import play.api.libs.json._
import play.modules.reactivemongo._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.collection.JSONCollection
import plex.{MovieState, API}
import security.Secured
import scala.concurrent.Future
import scala.util.Try

object MovieController extends Controller with Secured with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("movies")

  def index = withAuth { token => implicit request =>
    val movies = API.getMovies(token)
    Ok(views.html.movies(movies, token))
  }

  def movie(movieId: String) = withUserFuture { (user, token) => implicit request =>
    API.getMovie(movieId, token) match {
      case Some(movie) => {
        val findOffset = collection.find(Json.obj(
          "uid" -> user.uid,
          "movieId" -> movieId
        )).one[JsObject]

        findOffset.map { opt =>
          val offset = opt.map { obj =>
            (obj \ "offset").toString().toDouble
          }
          Ok(views.html.movie(movie, offset, token))
        }

      }
      case None => Future.apply(Ok("wut"))
    }
  }

  def watch(movieId: String, state: String, offset: Double) = withUserFuture { (user, token) => implicit request =>

    val futureUpdate = collection.update(
      Json.obj(
        "movieId" -> movieId,
        "uid" -> user.uid
      ),
      Json.obj(
        "movieId" -> movieId,
        "uid" -> user.uid,
        "offset" -> JsNumber(offset),
        "date" -> new Date
      ),
      upsert = true
    )

    futureUpdate.map { r =>
      Ok(Json.obj("success" -> r.ok))
    }
  }
}