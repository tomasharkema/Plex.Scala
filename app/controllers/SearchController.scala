package controllers

import play.api.libs.json.Json
import play.api.mvc.{Controller, Action}
import plex.API
import security.Secured
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by tomas on 28-04-15.
 */
object SearchController extends Controller with Secured {
  def movie(query: String) = withAuthFuture { token => implicit request =>
    API.getMovies(token, Some(query)).map { m =>
      println(m)
      Ok(Json.toJson(m)).as("application/json")
    }
  }
}
