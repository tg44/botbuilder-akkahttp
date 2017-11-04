package com.microsoft.bot.samples

import com.microsoft.bot.bot.ChatConnector
import com.microsoft.bot.dialogs.Dialog.Receive
import com.microsoft.bot.dialogs.{Dialog, Session}
import com.microsoft.bot.models.Activity

import scala.concurrent.Future

object EchoSample extends App with BaseSample {

  val ct = ChatConnector.withMicrosoftStorage(settings, classOf[EchoDialog])

}

class EchoDialog(val session: Session) extends Dialog {
  override def receive: Receive = {
    case msg: Activity if msg.header.`type` == Option("message") =>
      session.send(s"You said: ${msg.content.text.get}")
    case _ =>
      Future()
  }
}
