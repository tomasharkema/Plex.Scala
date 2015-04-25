package controllers

import java.util.Date

import play.api.libs.ws.WS
import play.api.mvc._
import play.api.libs.json._
import play.modules.reactivemongo._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.collection.JSONCollection
import plex.API
import security.Secured
import utils.SubtitlesUtils
import scala.concurrent.Future

object MovieController extends Controller with Secured with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("watching")

  def index = withUserFuture { (user, token) => implicit request =>
    val movies = API.getMovies(token)

    collection
      .find(Json.obj(
        "uid" -> user.uid
      ))
      .sort(Json.obj("date" -> -1))
      .cursor[JsObject]
      .collect[Seq]()
      .map { watchingJS =>
        val watching = watchingJS.flatMap { obj =>
          val mov = movies.find(p = _.key == (obj \ "movieId").as[String])
          mov.flatMap { m =>
            val offset = (obj \ "offset").as[Double]
            if (m.watchingProgress(Some(offset)).getOrElse(0f) < 0.9) {
              Some(m.copy(offset = Some(offset.toInt)))
            } else {
              None
            }
          }
        }
      println(watching)
        Ok(views.html.movies(watching, movies, token))
      }
  }

  def movie(movieId: String) = withUserFuture { (user, token) => implicit request =>
    API.getMovie(movieId, token) match {
      case Some(movie) =>
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
      case None => Future.apply(NotFound)
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

  def subtitles(movieId: String, lang: String) = withAuthFuture { token => implicit request =>
    API.getMovie(movieId, token) match {
      case Some(movie) =>
        import play.api.Play.current
        WS.url(movie.subtitles.find(_.languageCode == lang).head.url(token).toString()).get().map { res =>
          Ok(SubtitlesUtils.convertSRTtoVVT(res.body))
        }
      case None => Future.apply(NotFound)
    }
  }
}