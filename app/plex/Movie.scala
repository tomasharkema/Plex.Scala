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
                 duration: Int,
                // JS style (inc. millisecons)
                 offset:Option[Int]) {
  // getters
  def thumbUrl(token: String) = API.transcodeUrl(thumb, token, "photo")
  def artUrl(token: String) = API.endpoint + art & ("X-Plex-Token" -> token)

  def detailUrl = controllers.routes.MovieController.movie(key)

  private def off(offsetOverride: Option[Double] = None): Option[Double] = (offsetOverride, offset) match {
    case (Some(o), _) => Some(o.toDouble)
    case (None, Some(o)) => Some(o.toDouble)
    case (None, None) => None
  }

  def watchingOffset(offsetOverride: Option[Double] = None): Option[Double] = off(offsetOverride).flatMap { o =>
    if (watchingProgress(offsetOverride).getOrElse(0f) < 0.9)  Some(o) else None
  }

  def watchingProgress(offsetOverride: Option[Double] = None): Option[Float] = off(offsetOverride).map(o => o.toFloat / duration.toFloat)

  def stream(token: String, clientIp: String, offsetOverride: Option[Double] = None) = (API.clientEndpoint(clientIp) + media.head.parts.head.url & ("X-Plex-Token" -> token)) +
    watchingOffset(offsetOverride).map(o => "#t="+o).getOrElse("")

  def subtitles = media.head.subtitles

  // overrides
  override def toString = title
}

case class Media(resolution: String, parts: Seq[Part]) {
  def subtitles = parts.flatMap(_.stream).filter(_.codec == "srt").filter(_.languageCode != "")
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
      (el \ "@duration").text.toInt/1000,
      (el \ "@viewOffset").text.toIntOpt.map(_/1000)
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