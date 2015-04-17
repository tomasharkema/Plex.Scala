package plex

import scala.xml._

/**
 * Created by tomas on 12-04-15.
 */
case class Movie(title: String, thumb: String) {

  // getters
  def thumbUrl(token: String) = API.proxy(API.transcodeUrl(thumb, token, "photo"))

  // overrides
  override def toString = title
}

object Movie {

  // constructors
  def parseNode(el: Node) = Movie((el \ "@title").text, (el \ "@thumb").text)
}