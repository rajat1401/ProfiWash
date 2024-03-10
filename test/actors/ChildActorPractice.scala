package actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorPractice extends App {

  object WordCountMaster {
    case class Initialize(numChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }
  class WordCountMaster extends Actor {
    import WordCountMaster._
    override def receive: Receive = {
      case Initialize(numChildren) =>
        val children = (0 until numChildren).map(i => context.actorOf(Props[WordCountWorker], s"wcw$i"))
        context.become(withChildren(children, 0, 0, Map()))
    }

    def withChildren(children: Seq[ActorRef], childIndex: Int, taskId: Int, requestSenderMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] I have received task $taskId - I will send it to child $childIndex")
        val newRequestMap = requestSenderMap + (taskId -> sender())
        children(childIndex) ! WordCountTask(taskId, text)
        context.become(withChildren(children, (childIndex + 1)%children.size, taskId + 1, newRequestMap))
      case WordCountReply(id, count) =>
        requestSenderMap(id) ! WordCountReply(id, count)
        context.become(withChildren(children, (childIndex + 1)%children.size, taskId + 1, requestSenderMap - id))
    }
  }

  class WordCountWorker extends Actor {
    import WordCountMaster._
    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path} processing task $id")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {
    import WordCountMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCountMaster], "master")
        master ! Initialize(2)
        val countCheck = Seq("Hi Akka! Count the words in me.", "Dune 2 just released last week.", "Ye melody itni chocolaty kyu hai??")
        for (elem <- countCheck) {
          master ! elem
        }
      case WordCountReply(id, count) => println(s"Input $id has count $count")
    }
  }

  val actorSystem = ActorSystem("actorSystem")
  val testActor = actorSystem.actorOf(Props[TestActor], "testActor")
  testActor ! "go"

}
