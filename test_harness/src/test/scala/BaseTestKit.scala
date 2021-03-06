import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, ShouldMatchers}

class BaseTestKit(_system: ActorSystem) extends TestKit(_system)
with FlatSpecLike
with BeforeAndAfterAll
with ShouldMatchers
with ImplicitSender
with ScalaFutures {

  override protected def afterAll(): Unit = _system.terminate()
}
