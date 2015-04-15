package controllers

import play.api.mvc._

/**
 * Created by tomas on 15-04-15.
 */
object Login extends Controller {
  def login = Action {
    Ok("login")
  }
}
