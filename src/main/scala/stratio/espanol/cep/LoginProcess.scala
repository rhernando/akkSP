package stratio.espanol.cep

import akka.actor.Actor

/**
 * Created by rhernando on 18/05/15.
 */
class LoginProcess extends Actor with EventActor{
  def receive = {
    case msg: LoginMsg => {
      println("{} ACCESS", msg.username )
    }
  }
}

