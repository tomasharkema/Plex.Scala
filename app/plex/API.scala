package plex

import java.net.{URLEncoder, URL}

import com.netaporter.uri.Uri
import model._
import play.api.libs.ws.WS
import play.api.mvc.Results
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import com.netaporter.uri.dsl._

import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by tomas on 12-04-15.
 */
object API {
  private def local_host = "192.168.0.100"
  private def remote_host = "89.99.237.125"
  private def port = 32400

  var _host = ""
  private def host = {
    if (_host == "") {
      WS.url("https://plex.tv/pms/:/ip").get().map { res =>
        val hostRes = res.body
        if (hostRes.contains(remote_host)) {
          println("Host has same remote ip, choosing for: "+ local_host)
          _host = local_host
        } else {
          println("Host has foreign remote ip, choosing for: "+ remote_host)
          _host = remote_host
        }
        _host
      }
    } else {
      Future.apply(_host)
    }

  }

  def endpoint = Uri.parse(new URL("http", Await.result(host, Duration(10, "seconds")), port, "").toString)

  def proxy(path: Uri) = "/proxy" ? ("url" -> URLEncoder.encode(path toString(), "UTF-8"))

  def transcodeUrl(path: String, token:String, transcodeType:String) = endpoint / transcodeType + "/:" / "transcode" &
      ("X-Plex-Token" -> token) &
      ("url" -> ("http://127.0.0.1:32400" + path)) &
      ("width" -> 400) &
      ("height" -> 400) &
      ("minSize" -> 1)

  private def plexRequest(path: String)(implicit ec: ExecutionContext) = {
    import play.api.Play.current
    WS.url(path)
      .withHeaders(
        "X-Plex-Client-Identifier" -> "f4x08gwxi3a6ecdi",
        "X-Plex-Device" -> "OSX",
        "X-Plex-Device-Name" -> "Plex Web (Chrome)",
        "X-Plex-Platform" -> "Chrome",
        "X-Plex-Platform-Version" -> "41.0",
        "X-Plex-Product" -> "Plex Web",
        "X-Plex-Version" -> "2.3.24")
  }

  private def httpRequest(path: Uri, token: String)(implicit ec: ExecutionContext) = plexRequest(endpoint + path)
      .withHeaders("X-Plex-Token" -> token)

  def authentication(bearer:String): Future[Option[String]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    plexRequest("https://plex.tv/users/sign_in.xml")
      .withHeaders("Authorization" -> ("Basic " + bearer))
      .post(Results.EmptyContent())
      .map { response =>
        def token = (response.xml \ "authentication-token").text
        if (token == null || token.length == 0) {
          None
        } else {
          Some(token)
        }
      }
  }

  def defaultAuthenticated(path: String, token:String)(implicit ec: ExecutionContext) = httpRequest(path, token)

  def getUser(token: String): Future[Option[User]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    plexRequest("https://plex.tv/users/account").withHeaders("X-Plex-Token" -> token).get().map { response =>
      val user = response.xml \\ "user"
      user.map(User.parseUser).headOption
    }
  }

  def getMovies(token: String): Future[Seq[Movie]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    httpRequest("library/sections/1/all", token).get().map { response =>
      val movies = response.xml \\ "Video"
      movies.map(Movie.parseNode)
    }
  }

  def getMovie(movieId: String, token: String): Future[Option[Movie]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    httpRequest("library" / "metadata" / movieId & ("checkFiles" -> "1"), token).get().map { response =>
      val movie = response.xml \ "Video"
      movie.map(Movie.parseNode).headOption
    }
  }
}