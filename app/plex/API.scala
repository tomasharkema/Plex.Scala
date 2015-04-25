package plex

import java.net.{URLEncoder, URL}

import com.netaporter.uri.Uri
import model._
import play.api.libs.ws.WS
import play.api.mvc.Results
import scala.concurrent.{ExecutionContext, Future}
import scala.xml._
import com.netaporter.uri.dsl._
import scalaj.http.{HttpRequest, HttpResponse, Http}

/**
 * Created by tomas on 12-04-15.
 */
object API {
  //private def host = "192.168.0.100"
  private def host = "local.tomasharkema.nl"
  private def port = 32400

  def endpoint = Uri.parse(new URL("http", host, port, "").toString)

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
      .post(Results.EmptyContent()).map { response =>
      def token = (response.xml \ "authentication-token").text
      if (token == null || token.length == 0)
        None
      else
        Some(token)
    }
  }

  def defaultAuthenticated(path: String, token:String)(implicit ec: ExecutionContext) = httpRequest(path, token)

  def getUser(token: String): Future[Option[User]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    plexRequest("https://plex.tv/users/sign_in.xml").get().map { response =>
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