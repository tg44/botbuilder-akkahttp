package com.microsoft.bot.dialogs

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.microsoft.bot.bot.ChatConnector
import com.microsoft.bot.models.{Activity, ActivityAttachments, BotData, Content}
import com.microsoft.bot.storage.BotStorage
import com.typesafe.scalalogging.Logger
import spray.json.{DefaultJsonProtocol, JsArray}
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

case class SessionContext(connector: ChatConnector,
                          store: BotStorage,
                          coreDialogClass: Class[_ <: Dialog],
                          defaultHandler: PartialFunction[Activity, Future[Boolean]])

class Session(initialMessage: Activity,
              context: SessionContext)(implicit val actorSystem: ActorSystem, val materializer: Materializer, val executionContext: ExecutionContext)
    extends DefaultJsonProtocol {

  implicit val session: Session = this
  val logger = Logger(classOf[Session])

  //this will fail the session creation if the initial message is mailformed or not complete
  val channelId: String = initialMessage.header.channelId.get
  val conversationId: String = initialMessage.header.conversation.get.id.get
  val senderId: String = initialMessage.header.from.get.id.get

  def generateEtag(): String = UUID.randomUUID().toString.substring(0, 6)

  private val coreDialog = Dialog.deserialize(s"""{"name": "${context.coreDialogClass.getCanonicalName}", "data": {} }""".parseJson.asJsObject).get

  private var lastEtag = "*"
  private var cachedStack: List[Dialog] = List()

  private final def dialogStack: List[Dialog] = {
    val botData = context.store.getPrivateConversationData(channelId, conversationId, senderId)
    botData.fold(
      error => {
        logger.warn("Error in getPrivateConversationData : " + error.toString)
        List.empty[Dialog]
      },
      data => {
        handleCachedStackAtGet(data)
      }
    )
  }

  private def handleCachedStackAtGet(data: BotData) = {
    if (data.eTag == lastEtag) {
      cachedStack
    } else if (data.eTag == "*") {
      cachedStack
    } else {
      handleRecievedStack(data)
      cachedStack
    }
  }

  private final def dialogStack_=(value: List[Dialog]): Unit = {
    cachedStack = value
    val botData = BotData(lastEtag, Option(value.map(Dialog.serialize).toJson.asInstanceOf[JsArray]))
    val response = context.store.setPrivateConversationData(channelId, conversationId, senderId, botData)
    response.fold(
      error => logger.warn("Error in setPrivateConversationData : " + error.toString),
      data => handleRecievedStack(data)
    )
  }

  private def handleRecievedStack(data: BotData): Unit = {
    cachedStack = data.data
      .map(
        y =>
          y.elements
            .map(x => Dialog.deserialize(x.asJsObject))
            .filter(_.isDefined)
            .map(_.get)
            .toList
      )
      .getOrElse(List())
    lastEtag = data.eTag
  }

  final def !(msg: Activity): Future[Unit] = receiveMessage(msg)

  final def receiveMessage(msg: Activity): Future[Unit] = {
    context.defaultHandler(msg).flatMap { handled =>
      if (!handled) {
        val origin = dialogStack
        println(origin)
        if (origin.isEmpty) {
          coreDialog ! msg
        } else {
          origin.head ! msg
        }
      } else {
        Future.successful()
      }
    }
  }

  def emptyReplyActivity: Activity = {
    initialMessage.createReply()
  }

  def send(attachment: ActivityAttachments): Future[Unit] = {
    nonsafeSend(initialMessage.createReply().copy(content = Content(attachments = Option(Seq(attachment)))))
  }

  def sendChannelSpecificMessage(data: JsValue): Future[Unit] = {
    nonsafeSend(initialMessage.createReply().copy(content = Content(channelData = Option(data))))
  }

  def send(msg: String): Future[Unit] = {
    nonsafeSend(initialMessage.createReply(Option(msg)))
  }

  def send(msg: Activity): Future[Unit] = {
    nonsafeSend(msg.copy(header = initialMessage.createReplyHeader()))
  }

  def send(msg: Future[Activity]): Future[Unit] = {
    nonsafeSend(msg.map(_.copy(header = initialMessage.createReplyHeader())))
  }

  def sendTyping(): Future[Unit] = {
    nonsafeSend(emptyReplyActivity.copy(header = initialMessage.createReplyHeader(Option(Activity.TYPING))))
  }

  def beginDialog(dialogClass: Class[_ <: Dialog], data: JsValue): Unit = {
    val dialog = Dialog(dialogClass, data.asJsObject)
    dialogStack = dialog :: dialogStack
    dialog ! Dialog.AddedToStack
  }

  def replaceDialog(dialog: Dialog): Unit = {
    val original = dialogStack
    dialogStack = dialog :: (if (original.nonEmpty) original.tail else original)
    dialog ! Dialog.AddedToStack
  }

  def endDialog(msgToParent: Option[Any]): Unit = {
    val original = dialogStack
    dialogStack =
      if (original.isEmpty || original.tail.isEmpty) List.empty
      else original.tail

    original.headOption.foreach(_ ! Dialog.RemovedFromStack)
    if (dialogStack.isEmpty) {
      coreDialog ! Dialog.Resumed(msgToParent)
    } else {
      dialogStack.headOption.foreach(_ ! Dialog.Resumed(msgToParent))
    }
  }

  def reset(): Unit = {
    val original = dialogStack
    dialogStack = List.empty
    original.foreach(_ ! Dialog.RemovedFromStack)
  }

  def endConversation(): Unit = ???

  def save(): Unit = dialogStack = cachedStack

  private[this] def nonsafeSend(msg: Activity) = {
    context.connector.send(msg).map(_ => ())
  }

  private[this] def nonsafeSend(msg: Future[Activity]) = {
    context.connector.send(msg).map(_ => ())
  }

}
