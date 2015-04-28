package controllers

import com.netaporter.uri.dsl._
import play.api.libs.ws.WS
import play.api.mvc._
import plex.API
import security.Secured

object Proxy extends Controller with Secured {
  def proxy(url: String) = withAuthFuture { token => implicit request =>
    import play.api.Play.current
    import scala.concurrent.ExecutionContext.Implicits.global
    WS.url(API.endpoint / url).get().map { res =>
      Ok(res.body)
    }
  }
}
