import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer}

case class Work(name: String, time: Long = System.currentTimeMillis)

case class AddWork(work: Work)

case class WorkAdded(work: Work)

case object SnapShot

case object PrintState

case object ThrowException

case object GetState

case class WorkQueue(queue: List[Work] = Nil) {
  def added(event: WorkAdded): WorkQueue = copy(event.work :: queue)

  def size = queue.size

  override def toString = queue.reverse.toString
}

class SimplePersistentActor extends PersistentActor with ActorLogging {
  override def persistenceId: String = "simple_persistence_id"

  var workQueue = WorkQueue()

  def addWork(event: WorkAdded): Unit = workQueue = workQueue.added(event)

  def backlog = workQueue.size

  override def receiveRecover: Receive = {
    case event: WorkAdded => addWork(event)
    case SnapshotOffer(_, snapshot: WorkQueue) => workQueue = snapshot
  }

  override def receiveCommand: Receive = {
    case AddWork(work) => persist(WorkAdded(work))(addWork)
    case ThrowException => throw new Exception("The Persistent Actor is dying.")
    case SnapShot => saveSnapshot(workQueue)
    case PrintState => println(workQueue)
    case GetState => sender ! WorkQueue(workQueue.queue.reverse)
  }
}
