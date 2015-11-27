import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

case object Hello

class SimpleClusterListener extends Actor with ActorLogging {
  val cluster = Cluster(context.system)


  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive = {
    case Hello => sender ! "hello"

    case MemberUp(member) =>
      log.info("Member is up: {}", member.address)

    case UnreachableMember(member) =>
      log.warning("Member is detected unreachable: {}", member.address)

    case MemberRemoved(member, previousState) =>
      log.warning("Member is removed: {} after {}", member.address, previousState)

    case _: MemberEvent => //ignore
  }
}
