package actors

import akka.actor.{Props, ActorRef, Actor}
import play.api.Play.current

/**
 * Created by tomas on 22-04-15.
 */


object WatchingActor {
  def props(out: ActorRef) = Props(new WatchingActor(out))
}

class WatchingActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! ("I received your message: " + msg)
  }
}