package com.microsoft.bot.samples

import com.microsoft.bot.bot.ChatConnector
import com.microsoft.bot.dialogs.Dialog.{Receive, Resumed}
import com.microsoft.bot.dialogs._
import com.microsoft.bot.models.Activity

import scala.concurrent.Future

object MultipleDialogSample extends App with BaseSample {

  val ct = ChatConnector.withMicrosoftStorage(settings, classOf[MultipleDialogSampleRootDialog])

}
class MultipleDialogSampleRootDialog(val session: Session) extends Dialog with DefaultDialogsJsonSupport {
  import spray.json._
  override def receive: Receive = {
    case Resumed(x) if x.isEmpty =>
      session.send("error")
    case Resumed(x) =>
      session.send(x.get.toString)
    case msg: Activity if msg.header.`type` == Option("message") =>
      session.beginDialog(classOf[TwoOptionDialog], TwoOptionData("Worked?", "Yes", "No", 3).toJson)
      Future()
    case _ =>
      Future()
  }
}
