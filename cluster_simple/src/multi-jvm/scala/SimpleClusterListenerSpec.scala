import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers, WordSpecLike}

import scala.concurrent.duration._

object SimpleClusterListenerSpecConfig extends MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")

  nodeConfig(node1, node2) {
    ConfigFactory.parseString(
      s"""
      # Disable legacy metrics in akka-cluster.
      akka.cluster.metrics.enabled=off
      # Enable metrics extension in akka-cluster-metrics.
      # akka.extensions=["akka.cluster.metrics.ClusterMetricsExtension"]
      # Sigar native library extract location during tests.
      akka.cluster.metrics.native-library-extract-folder=target/native/${node1.name}
      """)
  }

  commonConfig(ConfigFactory.parseString(
    """
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.remote.log-remote-lifecycle-events = off
    // akka.cluster.roles = [compute]
     // router lookup config ...
    """))
}

class SimpleClusterListenerSpecMultiJvmNode1 extends SimpleClusterListenerSpec
class SimpleClusterListenerSpecMultiJvmNode2 extends SimpleClusterListenerSpec

abstract class SimpleClusterListenerSpec extends MultiNodeSpec(SimpleClusterListenerSpecConfig)
with WordSpecLike
with ShouldMatchers
with BeforeAndAfterAll
with ImplicitSender {

  import SimpleClusterListenerSpecConfig._

  override def initialParticipants: Int = roles.size

  "Illustrate how to start-up a cluster" in within(15.seconds) {
    Cluster(system).subscribe(testActor, classOf[MemberUp])
    expectMsgClass(classOf[CurrentClusterState])

    val node1Address = node(node1).address
    val node2Address = node(node2).address

    Cluster(system) join node1Address

    system.actorOf(Props[SimpleClusterListener], "clusterListener")

    receiveN(2).collect { case MemberUp(m) => m.address }.toSet should be(Set(node1Address, node2Address))

    Cluster(system).unsubscribe(testActor)
    testConductor.enter("all-up")
  }

  runOn(node1){
    "show that SimpleClusterListener receives message" in {
      val clusterListener = system.actorSelection(node(node1) / "user" / "clusterListener")
      clusterListener ! Hello
      expectMsg(100.millis, "hello")
    }
  }

  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}
