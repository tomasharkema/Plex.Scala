package plex

import java.net.{URLEncoder, URL}

import com.netaporter.uri.Uri
import model._
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

  private def plexRequest(path: String) = Http(path)
    .header("X-Plex-Client-Identifier", "f4x08gwxi3a6ecdi")
    .header("X-Plex-Device", "OSX")
    .header("X-Plex-Device-Name", "Plex Web (Chrome)")
    .header("X-Plex-Platform", "Chrome")
    .header("X-Plex-Platform-Version", "41.0")
    .header("X-Plex-Product", "Plex Web")
    .header("X-Plex-Version", "2.3.24")

  private def httpRequest(path: Uri, token: String) = plexRequest(endpoint + path)
      .header("X-Plex-Token", token)

  private def parse(res: HttpResponse[String]) = XML.loadString(res.body)

  private def authenticate(path: String, cl: (String) => (HttpRequest)) = {
    // TODO: n.encode64(n.toUtf8(a.username + ":" + a.password));
    val req = plexRequest("https://plex.tv/users/sign_in.xml")
      .method("POST")
      .header("Authorization", "Basic " + "")
      .asString

    val xml = parse(req)
    //token = (xml \ "authentication-token").text

    cl(path)
  }

  def authentication(bearer:String): Option[String] = {
    val req = plexRequest("https://plex.tv/users/sign_in.xml")
      .method("POST")
      .header("Authorization", "Basic " + bearer)
      .asString
    def token = (parse(req) \ "authentication-token").text
    if (token == null || token.length == 0)
      None
    else
      Some(token)
  }

  def request[T](path: String, token:String, requestClosure: (HttpRequest) => (HttpRequest), as:(HttpRequest) => (HttpResponse[T])): HttpResponse[T] = {
    val req = (path: String) => requestClosure(httpRequest(path, token))

    val res = as(req(path))

    if (res.is2xx) {
      res
    } else if (res.is4xx) {
      as(authenticate(path, req))
    } else {
      // handle other than 2xx error
      res
    }
  }

  def request(path: String, token:String) = request[String](path, token, _.copy(), _.asString)

  def defaultAuthenticated(path: String, token:String) = httpRequest(path, token)

  def getUser(token: String): Option[User] = {
    val xml = parse(plexRequest("https://plex.tv/users/account").header("X-Plex-Token", token).asString)
    val user = xml \\ "user"
    user.map(User.parseUser).headOption
  }

  def getMovies(token: String): Seq[Movie] = {
    val xml = parse(request("library/sections/1/all", token, _.header("a", "b"), _.asString))
    val movies = xml \\ "Video"
    movies.map(Movie.parseNode)
  }

  def getMovie(movieId: String, token: String): Option[Movie] = {
    val xml = request("library" / "metadata" / movieId & ("checkFiles" -> "1"), token)
    val movie = parse(xml) \ "Video"
    movie.map(Movie.parseNode).headOption
  }
}