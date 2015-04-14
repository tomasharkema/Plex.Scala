package plex

import scala.xml._

/**
 * Created by tomas on 12-04-15.
 */
case class Movie(name: String, art: String) {

  // constructors
  def this(el: Node) = this((el \ "@title").text, (el \ "@art").text)

  // getters
  def artUrl = API.proxy(art)

  // overrides
  override def toString = name
}
