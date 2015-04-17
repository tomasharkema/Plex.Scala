package plex

import scala.xml._
import com.netaporter.uri.dsl._

/**
 * Created by tomas on 12-04-15.
 */
case class Movie(title: String, thumb: String, key: String, media:Seq[Media]) {

  // getters
  def thumbUrl(token: String) = API.transcodeUrl(thumb, token, "photo")

  def detailUrl = controllers.routes.Application.movie(key)

  def getStream(token: String) = API.endpoint + media.head.parts.head.url & ("X-Plex-Token=" -> token)

  // overrides
  override def toString = title
}

case class Media(resolution: String, parts: Seq[Part])
case class Part(url: String)

object Movie {
  // constructors
  def parseNode(el: Node) = {
    Movie((el \ "@title").text, (el \ "@thumb").text, (el \ "@key").text.split("/").last, (el \ "Media").map(Media.parseNode))
  }

}

object Media {
  def parseNode(el: Node) = Media((el \ "@videoResolution").text, (el \\ "Part").map(Part.parseNode))
}

object Part {
  def parseNode(el: Node) = Part((el \ "@key").text)
}