package scala.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object DispatcherPractice extends App{

  class Counter extends Actor with ActorLogging {
    var count = 0
    override def receive: Receive = {
      case message =>
        count += 1
        log.info(s"[$count] $message")
    }
  }

  val system = ActorSystem("DispatcherPractice")
  val actors = for (i <- 1 to 10) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
  //can also do by config
//  val cleanuptestActor = system.actorOf(Props[Counter], "cleanuptest")

  val r = new scala.util.Random()
  for (i <- 1 to 500) {
    actors(r.nextInt(10)) ! i
  }


}
