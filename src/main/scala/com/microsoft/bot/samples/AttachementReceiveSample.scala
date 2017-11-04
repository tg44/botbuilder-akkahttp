package com.microsoft.bot.samples

import java.net.URL

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import com.microsoft.bot.bot.ChatConnector
import com.microsoft.bot.dialogs.{Dialog, Session}
import com.microsoft.bot.models.Activity

import scala.collection.immutable
import scala.concurrent.Future

object AttachementReceiveSample extends App with BaseSample {

  val ct: ChatConnector = ChatConnector.withMicrosoftStorage(settings, classOf[AttachementReceiveSampleRootDialog])

}

class AttachementReceiveSampleRootDialog(val session: Session) extends Dialog {

  override def receive = {
    case x: Activity => sendAttachement(x)
    case _ => Future()
  }

  val help =
    """
      |send me sth pls
    """.stripMargin

  def sendAttachement(message: Activity): Future[Unit] = {
    if (message.header.`type` == Option("message")) {
      val hasAttachement = message.content.attachments.isDefined
      val reply = if (hasAttachement) {
        val size = downloadAttachement(message)
        size.map(s => message.createReply(Option(s"downloaded $s byte")))
      } else {
        Future.successful(message.createReply(Option(help)))
      }

      session.send(reply).map(_ => ())
    } else Future.successful()
  }

  def downloadAttachement(message: Activity): Future[Long] = {
    if (message.content.attachments.get.isEmpty) {
      Future.successful(0)
    } else {
      val downloadable = message.content.attachments.get.head
      val headers = if (message.header.channelId == Option("skype") || message.header.channelId == Option("skype")) {
        if (new URL(downloadable.contentUrl.get).getHost.endsWith("skype.com")) {
          //var token = await new MicrosoftAppCredentials().GetTokenAsync();
          //httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
          /*immutable.Seq(
            RawHeader("User-Agent", USER_AGENT),
            RawHeader("Authorization", s"Bearer $token")
          )*/
          // todo skype token download
          immutable.Seq()
        } else {
          immutable.Seq()
        }
      } else {
        immutable.Seq()
      }

      val result = Http().singleRequest(
        HttpRequest(
          method = HttpMethods.GET,
          uri = downloadable.contentUrl.get,
          headers = headers
        )
      )
      result.flatMap(_.entity.dataBytes.runFold(0L)((x, z) => x + z.length))
    }
  }
}
