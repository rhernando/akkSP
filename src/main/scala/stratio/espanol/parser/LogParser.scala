package stratio.espanol.parser

import akka.actor.{Props, Actor}
import stratio.espanol.StreamFSM
import stratio.espanol.cep.{LoginMsg, LoginProcess}

/**
 * Created by rhernando on 18/05/15.
 */
class LogParser extends Actor {
  val format = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS")

  val login = context.actorOf(Props[LoginProcess])
  def receive = {
    case msg: Array[Byte] => {
      println("Got msg: " + new String(msg, "UTF-8"))
      val arrMessage = new String(msg, "UTF-8").split('|')

      println(arrMessage)

       arrMessage match {
        case Array(_, _, "LOGIN", _*) =>
          val Array(username, timestamp, _) = arrMessage
           login ! LoginMsg(username, format.parse(timestamp))
        case _ => ;
      }

      sender ! StreamFSM.Processed
    }

  }
}
