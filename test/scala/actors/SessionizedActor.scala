package actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, PoisonPill, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.DurationInt

object SessionizedActor extends App {

  class SessionActor extends Actor with ActorLogging {
    var schedule = setTimeout()
    def setTimeout(): Cancellable = {
      context.system.scheduler.scheduleOnce(3.seconds){
        self ! "timeout"
      }(context.system.dispatcher)
    }
    override def receive: Receive = {
      case "timeout" =>
        log.warning("Stopping myself")
        context.stop(self)
      case msg =>
        log.info(s"Received msg: ${msg.toString}")
        schedule.cancel()
        schedule = setTimeout()
    }
  }

  val system = ActorSystem("sessionTest")
  system.log.info("system started")
  val sessionActor = system.actorOf(Props[SessionActor], "doordie")
  system.scheduler.scheduleOnce(250.millis){
    sessionActor ! "Hello"
  }(system.dispatcher)
  system.scheduler.scheduleOnce(4.seconds) {
    sessionActor ! "still alive?"
  }(system.dispatcher)
  sessionActor ! "whatttt"

}
