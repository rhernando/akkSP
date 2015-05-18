package stratio.espanol

import akka.actor.{Props, ActorSystem}
import kafka.serializer.DefaultDecoder
import stratio.espanol.parser.LogParser

/**
 * Created by rhernando on 18/05/15.
 */
object Boot extends App {
  override def main(args: Array[String]): Unit = {

    val system = ActorSystem("elespanol")
    val parserActor = system.actorOf(Props[LogParser])


    /*
the consumer will have 4 streams and max 64 messages per stream in flight, for a total of 256
concurrently processed messages.
*/
    val consumerProps = AkkaConsumerProps.forSystem(
      system = system,
      zkConnect = "localhost:2181",
      topic = "elespanol-topic",
      group = "stratio-group",
      streams = 4, //one per partition
      keyDecoder = new DefaultDecoder(),
      msgDecoder = new DefaultDecoder(),
      receiver = parserActor
    )
    val consumer = new AkkaConsumer(consumerProps)
    consumer.start()  //returns a Future[Unit] that completes when the connector is started

    consumer.commit() //returns a Future[Unit] that completes when all in-flight messages are processed and offsets are committed.

   // consumer.stop()   //returns a Future[Unit] that completes when the connector is stopped.

  }
}
