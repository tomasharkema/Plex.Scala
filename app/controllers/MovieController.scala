package controllers

import java.util.Date

import actors.WatchingActor
import model.User
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.libs.json._
import play.modules.reactivemongo._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.json.collection.JSONCollection
import plex.{Movie, API}
import security.Secured
import utils.SubtitlesUtils
import scala.concurrent.Future
import play.api.Play.current

object MovieController extends Controller with Secured with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("watching")

  private def getWatchingMovies(movies: Seq[Movie], user: User): Future[Seq[Movie]] =
    collection.find(Json.obj("uid" -> user.uid))
          .sort(Json.obj("date" -> -1))
          .cursor[JsObject]
          .collect[Seq]()
          .map { watchingJS =>
      watchingJS.flatMap { obj =>
        val offset = (obj \ "offset").as[Double]
        val mov = movies.find(p = _.key == (obj \ "movieId").as[String])
        mov.flatMap { m =>
          if (m.watchingProgress(Some(offset)).getOrElse(0f) < 0.9) {
            Some(m.copy(offset = Some(offset.toInt)))
          } else {
            None
          }
        }
      }
  }

  def index = withUserFuture { (user, token) => implicit request =>
    API.getMovies(token).map { movies =>
      getWatchingMovies(movies, user).map { watching =>
        Ok(views.html.movies(watching, movies, token))
      }
    }.flatMap(r => r)
  }

  def movie(movieId: String) = withUserFuture { (user, token) => implicit request =>
    API.getMovie(movieId, token).map {
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
    }.flatMap(r => r)
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

  def socket = WebSocket.tryAcceptWithActor[String, String] { request =>
    Future.successful(request.session.get("user") match {
      case None => Left(Forbidden)
      case Some(_) => Right(WatchingActor.props)
    })
  }

  def subtitles(movieId: String, lang: String) = withAuthFuture { token => implicit request =>
    API.getMovie(movieId, token).map {
      case Some(movie) => {
        import play.api.Play.current
        WS.url(movie.subtitles.find(_.languageCode == lang).head.url(token).toString()).get().map { res =>
          Ok(SubtitlesUtils.convertSRTtoVVT(res.body))
        }
      }
      case None => Future.apply(NotFound)
    }.flatMap(r => r)
  }
}