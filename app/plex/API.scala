package plex

import java.net.{URLEncoder, URL}

import model.Auth

import scala.util.Try
import scala.xml._
import scalaj.http
import scalaj.http.{HttpRequest, HttpResponse, Http}

/**
 * Created by tomas on 12-04-15.
 */
object API {
  //private def host = "192.168.0.100"
  private def host = "local.tomasharkema.nl"
  private def port = 32400

  def endpoint:String => String = new URL("http", host, port, _).toString

  def proxy(path: String) = "/proxy?url=" + URLEncoder.encode(path, "UTF-8")

  def transcodeUrl(path: String, token:String, transcodeType:String) = "/" + transcodeType + "/:/transcode?X-Plex-Token=" + token + "&url=" + URLEncoder.encode("http://127.0.0.1:32400" + path, "UTF-8")

  private def plexRequest(path: String) = Http(path)
    .header("X-Plex-Client-Identifier", "f4x08gwxi3a6ecdi")
    .header("X-Plex-Device", "OSX")
    .header("X-Plex-Device-Name", "Plex Web (Chrome)")
    .header("X-Plex-Platform", "Chrome")
    .header("X-Plex-Platform-Version", "41.0")
    .header("X-Plex-Product", "Plex Web")
    .header("X-Plex-Version", "2.3.24")

  private def httpRequest(path: String, token: String) = plexRequest(endpoint(path))
    .header("X-Plex-Token", token)

  private def parse(res: HttpResponse[String]): Elem = {
    XML.loadString(res.body)
  }

  private def authenticate(path: String, cl: (String) => (HttpRequest)): HttpRequest = {
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
    println(req)
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

  def defaultAuthenticated(path: String, token:String) = httpRequest(path, token)

  def getMovies(token:String): Seq[Movie] = {
    val moviesReq = request("/library/sections/1/all", token, _.header("a", "b"), _.asString)
    val xml = parse(moviesReq)
    val movies = xml \\ "Video"

    movies.map(Movie.parseNode)
  }

}