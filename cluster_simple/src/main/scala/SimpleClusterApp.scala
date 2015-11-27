import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object SimpleClusterApp {
  def main(args: Array[String]) {
    val system = ActorSystem("ClusterSystem", ConfigFactory.load())
    system.actorOf(Props[SimpleClusterListener], "clusterListener")
  }
}
