package actors

import akka.actor.Actor


object StatelessCounter {
  case class Increment(amount: Int)
  case class Decrement(amount: Int)
  case object PrintCount

}
class StatelessCounter extends Actor {
  import StatelessCounter._
  override def receive: Receive = receiveCounter(0)

  def receiveCounter(i: Int): Receive = {
    case Increment(amount) =>
      println(s"Incrementing counter by $amount")
      context.become(receiveCounter(i + amount))
    case Decrement(amount) =>
      println(s"Decrementing counter by $amount")
      context.become(receiveCounter(i - amount))
    case PrintCount =>
      println(s"Current count is $i")
  }
}
