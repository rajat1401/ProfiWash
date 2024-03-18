package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object VotingExercise extends App {

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case object MustVoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])
  case class AggregateVotes(citizens: Set[ActorRef])


  class Citizen extends Actor {
    override def receive: Receive = notYetVoted

    def notYetVoted: Receive = {
      case Vote(candidate) => context.become(voted(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
      case MustVoteStatusRequest => sender() ! VoteStatusReply(Some("NA"))
    }

    def voted(candidate: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  class VoteAggregator extends Actor {
    var currentStats: Map[String, Int] = Map()
    var stillwaiting: Set[ActorRef] = Set()

    override def receive: Receive = {
      case AggregateVotes(citizens) =>
        stillwaiting = citizens
        citizens.foreach(_ ! VoteStatusRequest)
      case VoteStatusReply(None) => sender() ! MustVoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        stillwaiting = stillwaiting - sender()
        currentStats = currentStats + (candidate -> (currentStats.getOrElse(candidate, 0) + 1))
        if(stillwaiting.isEmpty){
          println(s"${context.self}" + currentStats)
        }
    }
  }

  class VoteAggregatorStateless extends Actor {
    override def receive: Receive = awaitingAggregate

    def awaitingAggregate: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(_ ! VoteStatusRequest)
        context.become(awaitingStatusReplies(citizens, Map()))
    }

    def awaitingStatusReplies(citizens: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => sender() ! MustVoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStats = currentStats.++(Map(candidate -> (currentStats.getOrElse(candidate, 0) + 1)))
        val waitOnCitizens = citizens - sender()
        if(waitOnCitizens.isEmpty){
          println("Election concluded!!! Results below:-")
          println("**************************************")
          println(s"${context.self}" + newStats)
        }else {
          context.become(awaitingStatusReplies(citizens - sender(), newStats))
        }
    }
  }

  //testing voting system
  val actorSystem = ActorSystem("votingSystem")
   val alice = actorSystem.actorOf(Props[Citizen])
   val bob = actorSystem.actorOf(Props[Citizen])
   val charlie = actorSystem.actorOf(Props[Citizen])
   val daniel = actorSystem.actorOf(Props[Citizen])

  alice ! Vote("Mom")
  bob ! Vote("Dad")
  charlie ! Vote("Mom")

  val voteAggregator = actorSystem.actorOf(Props[VoteAggregator])
  val voteAggregatorStateless = actorSystem.actorOf(Props[VoteAggregatorStateless])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))
  voteAggregatorStateless ! AggregateVotes(Set(alice, bob, charlie, daniel))

}
