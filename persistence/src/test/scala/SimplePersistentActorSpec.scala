import akka.actor.{ActorSystem, Props}
import scala.concurrent.duration._

class SimplePersistentActorSpec extends TestKitBase(ActorSystem("persistence-test")) {
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
        /**
          * todo
          * 1. use in-memory db
          * 2. assert order of jobs
          * 3. create test-harness project
          */
        case WorkQueue(backlog) => println(backlog)
      }
    }
  }
}

