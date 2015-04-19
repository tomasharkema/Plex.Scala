package model

import plex.Movie

import scala.xml._

/**
 * Created by tomas on 16-04-15.
 */

case class UserForm(username: String, password: String)

case class User(uid: String, email: String, username: String)
object User {
  def parseUser(el: Node) = User((el \ "@id").text, (el \ "@email").text, (el \ "@username").text)
}