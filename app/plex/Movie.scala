package plex

import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.{MongoConnection, MongoDriver}

import scala.xml._
import com.netaporter.uri.dsl._
import utils.StringUtils._

/**
 * Created by tomas on 12-04-15.
 */
case class Movie(title: String,
                 art: String,
                 thumb: String,
                 key: String,
                 media:Seq[Media],
                 description: String,
                // JS style (inc. millisecons)
                 offset:Option[Int]) {
  // getters
  def thumbUrl(token: String) = API.transcodeUrl(thumb, token, "photo")
  def artUrl(token: String) = API.endpoint + art & ("X-Plex-Token" -> token)

  def detailUrl = controllers.routes.MovieController.movie(key)

  def stream(token: String, offsetOverride: Option[Double] = None) = (API.endpoint + media.head.parts.head.url & ("X-Plex-Token" -> token)) +
    (offsetOverride match {
      case Some(o) => "#t=" + o
      case None => offset match {
        case Some(o) => "#t=" + o/1000
        case None => ""
      }
    })

  def subtitles = media.head.subtitles

  // overrides
  override def toString = title
}

case class Media(resolution: String, parts: Seq[Part]) {
  def subtitles = parts.flatMap(_.stream).filter(_.codec == "srt")
}
case class Part(url: String, stream: Seq[Stream])
case class Stream(codec: String, key: String, language: String, languageCode: String) {
  def url(token: String) = API.endpoint + key & ("X-Plex-Token" -> token)
  def endpoint(movie: String) = controllers.routes.MovieController.subtitles(movie, languageCode)
}

case class MovieState(user: String, offset: Double)

object Movie {
  def parseNode(el: Node) = {
    Movie(
      (el \ "@title").text,
      (el \ "@art").text,
      (el \ "@thumb").text,
      (el \ "@key").text.split("/").last,
      (el \ "Media").map(Media.parseNode),
      (el \ "@summary").text,
      (el \ "@viewOffset").text.toIntOpt
    )
  }
}

object Media {
  def parseNode(el: Node) = Media((el \ "@videoResolution").text, (el \\ "Part").map(Part.parseNode))
}

object Part {
  def parseNode(el: Node) = Part((el \ "@key").text, (el \\ "Stream").map(Stream.parseNode))
}

object Stream {
  def parseNode(el: Node) = Stream((el \ "@codec").text, (el \ "@key").text, (el \ "@language").text, (el \ "@languageCode").text)
}