package controllers

import com.netaporter.uri.dsl._
import play.api.mvc._
import plex.API
import security.Secured

import scalaj.http.Http

object Proxy extends Controller with Secured {
  def proxy(url: String) = withAuth { token => implicit request =>
    Ok(Http(API.endpoint / url).timeout(0, 0).asBytes.body)
  }
}
