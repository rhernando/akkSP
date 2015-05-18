package stratio.espanol.cep

import java.util.Date

import scala.util.Random

/**
 * Created by rhernando on 18/05/15.
 */
trait EventActor {
  def user(username: String): User ={
    User(username, Random.nextLong())
  }
}

/* DOMAIN */
case class User(username:String, id: Long)
case class Post(title:String, id: Long)
case class Comment(title:String, id: Long, postId:Long)

/* MESSAGES */
case class LoginMsg(username:String, timestamp: Date)
case class AccessMsg(username:String, timestamp: Date, postId: Long)
case class ShareMsg(username:String, timestamp: Date, postId: Long)
case class LikeMsg(username:String, timestamp: Date, postId: Long)
case class CommentMsg(username:String, timestamp: Date, postId: Long, commentId:Long)
