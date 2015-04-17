package plex

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
                 offset:Option[Int]) {
  // getters
  def thumbUrl(token: String) = API.transcodeUrl(thumb, token, "photo")
  def artUrl(token: String) = API.endpoint + art & ("X-Plex-Token" -> token)

  def detailUrl = controllers.routes.Application.movie(key)

  def getStream(token: String) = (API.endpoint + media.head.parts.head.url & ("X-Plex-Token" -> token)) +
    (offset match {
      case Some(o) => "#t=" + o/1000
      case None => ""
    })

  // overrides
  override def toString = title
}

case class Media(resolution: String, parts: Seq[Part])
case class Part(url: String)

object Movie {
  // constructors
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
  def parseNode(el: Node) = Part((el \ "@key").text)
}