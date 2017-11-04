package com.microsoft.bot.samples

import java.nio.file.{Files, Paths}
import java.util.Base64

import com.microsoft.bot.bot.ChatConnector
import com.microsoft.bot.dialogs.{Dialog, Session}
import com.microsoft.bot.models.{Activity, ActivityAttachments, Content}

import scala.concurrent.Future

object AttachementSendSample extends App with BaseSample {

  val ct: ChatConnector = ChatConnector.withMicrosoftStorage(settings, classOf[AttachementSendSampleRootDialog])

}

class AttachementSendSampleRootDialog(val session: Session) extends Dialog {
  override def receive = {
    case x: Activity => sendAttachement(x)
    case _ => Future()
  }

  val help =
    """
      |1 ShowInlineAttachment
      |2 ShowUploadedAttachment
      |3 ShowInternetAttachment
    """.stripMargin

  def sendAttachement(message: Activity): Future[Unit] = {
    if (message.header.`type` == Option("message")) {
      val msgText = message.content.text.get

      val reply: Activity = msgText match {
        case "1" => inlineAttachement(message)
        case "2" => message.createReply(Option("not supported yet :(")) // todo need connector client to upload the img
        case "3" => internetAttachement(message)
        case _ => message.createReply(Option(help))

      }

      session.send(reply).map(_ => ())
    } else Future.successful()
  }

  def inlineAttachement(message: Activity): Activity = {
    println(Paths.get("").toAbsolutePath.toString)
    val byteArray = Files.readAllBytes(Paths.get("images/small-image.png"))
    val imageData = Base64.getEncoder.encodeToString(byteArray)
    val attachement = ActivityAttachments(
      name = Option("small-image.png"),
      contentType = Option("image/png"),
      contentUrl = Option(s"data:image/png;base64,${imageData}"),
      thumbnailUrl = None,
      content = None
    )
    message.createReply().copy(content = Content(attachments = Option(Seq(attachement))))
  }

  def internetAttachement(message: Activity) = {
    val attachement = ActivityAttachments(
      name = Option("BotFrameworkOverview.png"),
      contentType = Option("image/png"),
      contentUrl = Option("https://docs.microsoft.com/en-us/bot-framework/media/how-it-works/architecture-resize.png"),
      thumbnailUrl = None,
      content = None
    )
    message.createReply().copy(content = Content(attachments = Option(Seq(attachement))))
  }

}
