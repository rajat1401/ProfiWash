package actors

import actors.ActorPractice.Person.LiveTheLife
import akka.actor.{Actor, ActorRef, Props}

object ActorPractice extends App {

  object Person {
    case object Success
    case object Failure
    case class LiveTheLife(account: ActorRef)

  }

  object BankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object PrintStatement

    def props(balance: Int): Props = Props(new BankAccount(balance))
  }

  class Person extends Actor {
    import Person._
    import BankAccount._
    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(9000000)
        account ! Withdraw(500)
        account ! PrintStatement
      case Success => println("Operation was a success")
      case Failure => println("Operation failed")
    }
  }

  class BankAccount(bbalance: Int) extends Actor {
    import BankAccount._
    import Person._

    var balance = bbalance

    override def receive: Receive = {
      case Deposit(amount) =>
        if(amount > 0) {
          balance += amount
          sender() ! Success
        } else{
          sender() ! Failure
        }
      case Withdraw(amount) =>
        if(balance >= amount) {
          balance -= amount
          sender() ! Success
        } else {
          sender() ! Failure
        }
      case PrintStatement => println(s"Your balance is $balance")
    }
  }

  val actorSystem = akka.actor.ActorSystem("bankAccountDemo")
  val bob = actorSystem.actorOf(Props[Person])
  val sbi = actorSystem.actorOf(BankAccount.props(12))
  bob ! LiveTheLife(sbi)

}
