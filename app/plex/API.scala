package plex

import java.net.URL

import scala.xml._
import scalaj.http.{HttpRequest, HttpResponse, Http}

/**
 * Created by tomas on 12-04-15.
 */
object API {
  private def host = "192.168.0.100"
  //private def host = "local.tomasharkema.nl"
  private def port = 32400

  private def endpoint(path: String) = new URL("http", host, port, path).toString

  var token = ""

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
      .header("Authorization", "Basic " + bearer)
      .asString

    val xml = parse(req)
    val authToken = xml \ "authentication-token"
    token = authToken.text

    cl(path)
  }

  private def request(path: String, requestClosure: (HttpRequest) => (HttpRequest)): HttpResponse[String] = {
    val req = (path: String) => {
      requestClosure(httpRequest(path, token))
    }

    val res = req(path).asString

    if (res.is2xx) {
      res
    } else if (res.is4xx) {
      authenticate(path, req).asString
    } else {
      res
    }
  }

  def getMovies: Seq[Movie] = {
    println("getMovies")
    val moviesReq = request("/library/sections/1/all", (req: HttpRequest) => req.header("a", "b"))
    val xml = parse(moviesReq)

    val movies = xml \\ "Video"

    movies.map { el =>
      new Movie((el \ "@title").text)
    }
  }

}