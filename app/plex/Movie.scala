package plex

import scala.xml._

/**
 * Created by tomas on 12-04-15.
 */
case class Movie(title: String, thumb: String) {

  // constructors
  def this(el: Node) = this((el \ "@title").text, (el \ "@thumb").text)

  // getters
  def thumbUrl = API.proxy(API.transcodeUrl(thumb, "photo"))

  // overrides
  override def toString = title
}
