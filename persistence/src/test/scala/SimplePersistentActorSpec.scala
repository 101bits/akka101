import akka.actor.{ActorSystem, Props}
import scala.concurrent.duration._

class SimplePersistentActorSpec extends BaseTestKit(ActorSystem("persistence-test")) {
  behavior of "PersistenceActor"

  "Work" should "be added in order" in {
    val persistentActor = system.actorOf(Props[SimplePersistentActor], "simplePersistentActor")

    within(2.second) {
      persistentActor ! AddWork(Work("file1"))
      persistentActor ! AddWork(Work("file2"))
      persistentActor ! AddWork(Work("file3"))
      persistentActor ! ThrowException
      persistentActor ! AddWork(Work("file4"))
      persistentActor ! AddWork(Work("file5"))

      persistentActor ! GetState
      expectMsgPF() {
        case WorkQueue(backlog) =>
          backlog.length shouldBe 5
          backlog.map(_.name) should equal(List("file1", "file2", "file3", "file4", "file5"))
      }
    }
  }
}

