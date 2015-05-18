package stratio.espanol

import akka.actor._
import akka.pattern._
import akka.util.Timeout
import collection.JavaConverters._
import concurrent.Future
import concurrent.duration._
import java.util.Properties
import kafka.consumer.{TopicFilter, ConsumerConfig, Consumer}
import kafka.serializer.Decoder
import kafka.message.MessageAndMetadata

/**
 * Created by rhernando on 18/05/15.
 */
object AkkaConsumer{
  def toProps(props:collection.mutable.Set[(String,String)]): Properties = {
    props.foldLeft(new Properties()) {
      case (p, (k, v)) =>
        p.setProperty(k, v)
        p
    }
  }
}

class AkkaConsumer[Key,Msg](props:AkkaConsumerProps[Key,Msg]) {

  import AkkaConsumer._

  lazy val connector = createConnection(props)

  def kafkaConsumerProps(zkConnect:String, groupId:String) = {
    val consumerConfig = props.system.settings.config.getConfig("kafka.consumer")
    val consumerProps = consumerConfig.entrySet().asScala.map{
      entry => entry.getKey -> consumerConfig.getString(entry.getKey)
    } ++ Set("zookeeper.connect" -> zkConnect, "group.id" -> groupId)
    toProps(consumerProps)
  }

  def kafkaConsumer(zkConnect:String, groupId:String) = {
    Consumer.create(new ConsumerConfig(kafkaConsumerProps(zkConnect, groupId)))
  }

  def createConnection(props:AkkaConsumerProps[Key,Msg]) =  {
    import props._
    val consumerConfig = new ConsumerConfig(kafkaConsumerProps(zkConnect, group))
    val consumerConnector = Consumer.create(consumerConfig)
    props.connectorActorName.map{
      name =>  actorRefFactory.actorOf(Props(new ConnectorFSM(props, consumerConnector)), name)
    }.getOrElse(actorRefFactory.actorOf(Props(new ConnectorFSM(props, consumerConnector))))
  }

  def start():Future[Unit] = {
    import props.system.dispatcher
    (connector ? ConnectorFSM.Start)(props.startTimeout).map{
      started =>
        props.system.log.info("at=consumer-started")
    }
  }

  def stop():Future[Unit] = {
    import props.system.dispatcher
    (connector ? ConnectorFSM.Stop)(props.startTimeout).map{
      stopped =>
        props.system.log.info("at=consumer-stopped")
    }
  }

  def commit():Future[Unit] = {
    import props.system.dispatcher
    (connector ? ConnectorFSM.Commit)(props.commitConfig.commitTimeout).map{
      committed =>
        props.system.log.info("at=consumer-committed")
    }
  }
}

object AkkaConsumerProps {
  def forSystem[Key, Msg](system: ActorSystem,
                          zkConnect: String,
                          topic: String,
                          group: String,
                          streams: Int,
                          keyDecoder: Decoder[Key],
                          msgDecoder: Decoder[Msg],
                          receiver: ActorRef,
                          msgHandler: (MessageAndMetadata[Key,Msg]) => Any = defaultHandler[Key, Msg],
                          connectorActorName:Option[String] = None,
                          maxInFlightPerStream: Int = 64,
                          startTimeout: Timeout = Timeout(5 seconds),
                          commitConfig: CommitConfig = CommitConfig()): AkkaConsumerProps[Key, Msg] =
    AkkaConsumerProps(system, system, zkConnect, Right(topic), group, streams, keyDecoder, msgDecoder, msgHandler, receiver, connectorActorName, maxInFlightPerStream, startTimeout, commitConfig)

  def forSystemWithFilter[Key, Msg](system: ActorSystem,
                                    zkConnect: String,
                                    topicFilter: TopicFilter,
                                    group: String,
                                    streams: Int,
                                    keyDecoder: Decoder[Key],
                                    msgDecoder: Decoder[Msg],
                                    receiver: ActorRef,
                                    msgHandler: (MessageAndMetadata[Key,Msg]) => Any = defaultHandler[Key, Msg],
                                    connectorActorName:Option[String] = None,
                                    maxInFlightPerStream: Int = 64,
                                    startTimeout: Timeout = Timeout(5 seconds),
                                    commitConfig: CommitConfig = CommitConfig()): AkkaConsumerProps[Key, Msg] =
    AkkaConsumerProps(system, system, zkConnect, Left(topicFilter), group, streams, keyDecoder, msgDecoder, msgHandler, receiver, connectorActorName, maxInFlightPerStream, startTimeout, commitConfig)


  def forContext[Key, Msg](context: ActorContext,
                           zkConnect: String,
                           topic: String,
                           group: String,
                           streams: Int,
                           keyDecoder: Decoder[Key],
                           msgDecoder: Decoder[Msg],
                           receiver: ActorRef,
                           msgHandler: (MessageAndMetadata[Key,Msg]) => Any = defaultHandler[Key, Msg],
                           connectorActorName:Option[String] = None,
                           maxInFlightPerStream: Int = 64,
                           startTimeout: Timeout = Timeout(5 seconds),
                           commitConfig: CommitConfig): AkkaConsumerProps[Key, Msg] =
    AkkaConsumerProps(context.system, context, zkConnect, Right(topic), group, streams, keyDecoder, msgDecoder, msgHandler, receiver,connectorActorName, maxInFlightPerStream, startTimeout, commitConfig)

  def forContextWithFilter[Key, Msg](context: ActorContext,
                                     zkConnect: String,
                                     topicFilter: TopicFilter,
                                     group: String,
                                     streams: Int,
                                     keyDecoder: Decoder[Key],
                                     msgDecoder: Decoder[Msg],
                                     receiver: ActorRef,
                                     msgHandler: (MessageAndMetadata[Key,Msg]) => Any = defaultHandler[Key, Msg],
                                     connectorActorName:Option[String] = None,
                                     maxInFlightPerStream: Int = 64,
                                     startTimeout: Timeout = Timeout(5 seconds),
                                     commitConfig: CommitConfig): AkkaConsumerProps[Key, Msg] =
    AkkaConsumerProps(context.system, context, zkConnect, Left(topicFilter), group, streams, keyDecoder, msgDecoder, msgHandler, receiver,connectorActorName, maxInFlightPerStream, startTimeout, commitConfig)

  def defaultHandler[Key,Msg]: (MessageAndMetadata[Key,Msg]) => Any = msg => msg.message()
}

case class AkkaConsumerProps[Key,Msg](system:ActorSystem,
                                      actorRefFactory:ActorRefFactory,
                                      zkConnect:String,
                                      topicFilterOrTopic:Either[TopicFilter,String],
                                      group:String,
                                      streams:Int,
                                      keyDecoder:Decoder[Key],
                                      msgDecoder:Decoder[Msg],
                                      msgHandler: (MessageAndMetadata[Key,Msg]) => Any,
                                      receiver: ActorRef,
                                      connectorActorName:Option[String],
                                      maxInFlightPerStream:Int = 64,
                                      startTimeout:Timeout = Timeout(5 seconds),
                                      commitConfig:CommitConfig = CommitConfig())

case class CommitConfig(commitInterval:Option[FiniteDuration] = Some(10 seconds),
                        commitAfterMsgCount:Option[Int] = Some(10000),
                        commitTimeout:Timeout = Timeout(5 seconds)
                         )