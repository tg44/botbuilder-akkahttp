package com.microsoft.bot.bot

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.microsoft.bot.dialogs.{Dialog, Session, SessionContext}
import com.microsoft.bot.models.Activity
import com.microsoft.bot.storage.BotStorage

import collection._
import scala.concurrent.{ExecutionContext, Future}

class SessionStore(context: SessionContext)(implicit actorSystem: ActorSystem, materializer: Materializer, executionContext: ExecutionContext) {

  //todo change it to some guava concurent cache
  //channel, user, conversation
  val cache = mutable.Map[String, mutable.Map[String, mutable.Map[String, Session]]]()

  def routeActivity(msg: Activity): Future[Unit] = {
    getByActivity(msg) ! msg
  }

  def getByActivity(msg: Activity): Session = {
    val firstHit = cache.get(msg.header.channelId.get)
    if (firstHit.isDefined) {
      val secondHit = firstHit.get.get(msg.header.from.get.id.get)
      if (secondHit.isDefined) {
        val thirdHit = secondHit.get.get(msg.header.conversation.get.id.get)
        if (thirdHit.isDefined) {
          thirdHit.get
        } else {
          val session = new Session(msg, context)
          secondHit.get.put(msg.header.conversation.get.id.get, session)
          session
        }
      } else {
        val session = new Session(msg, context)
        firstHit.get.put(msg.header.from.get.id.get, mutable.Map(msg.header.conversation.get.id.get -> session))
        session
      }
    } else {
      val session = new Session(msg, context)
      cache.put(msg.header.channelId.get, mutable.Map(msg.header.from.get.id.get -> mutable.Map(msg.header.conversation.get.id.get -> session)))
      session
    }
  }
}
