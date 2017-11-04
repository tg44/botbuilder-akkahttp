package com.microsoft.bot.dialogs

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect._

object Dialog extends DefaultJsonProtocol {
  final val emptyBehaviorStack: List[Dialog.Receive] = Nil
  type Receive = PartialFunction[Any, Future[Unit]]
  type DialogClass = Class[_ <: Dialog]

  case object AddedToStack

  case object RemovedFromStack

  case class Resumed(msgToParent: Option[Any])

  private val loader = ClassLoader.getSystemClassLoader

  def apply(dialogClass: Class[_ <: Dialog], data: JsObject)(implicit session: Session): Dialog = {
    val instance = dialogClass.getConstructors()(0).newInstance(session).asInstanceOf[Dialog]
    instance.deserialize(data)
    instance
  }

  def serialize(dialog: Dialog): JsObject = {
    val data = dialog.serialize()
    val name = dialog.getClass.getCanonicalName
    JsObject(
      "name" -> JsString(name),
      "data" -> data
    )
  }

  def deserialize(obj: JsObject)(implicit session: Session): Option[Dialog] = {
    for {
      className <- obj.fields.get("name")
      data = obj.fields("data").asJsObject
      clazz <- findClass(className.convertTo[String])
      instance = clazz.getConstructors()(0).newInstance(session).asInstanceOf[Dialog]
      _ = instance.deserialize(data)
    } yield instance
  }

  private def findClass(className: String): Option[DialogClass] = {
    val clazz: Class[_] = java.lang.Class.forName(className, false, loader)
    if (classOf[Dialog].isAssignableFrom(clazz)) {
      Option(clazz.asInstanceOf[DialogClass])
    } else {
      None
    }
  }

}

trait Dialog extends SprayJsonSupport with DefaultJsonProtocol {

  private[this] var behaviorStack: List[Dialog.Receive] = receive :: Dialog.emptyBehaviorStack

  final def receiveMessage(msg: Any): Future[Unit] = behaviorStack.head(msg)

  final def become(behavior: Dialog.Receive, discardOld: Boolean = true): Unit =
    behaviorStack = behavior :: (if (discardOld && behaviorStack.nonEmpty) behaviorStack.tail else behaviorStack)

  final def unbecome(): Unit = {
    val original = behaviorStack
    behaviorStack =
      if (original.isEmpty || original.tail.isEmpty) receive :: Dialog.emptyBehaviorStack
      else original.tail
  }

  final def !(msg: Any): Future[Unit] = receiveMessage(msg)

  implicit val actorSystem = session.actorSystem
  implicit val materializer = session.materializer
  implicit val executionContext = session.executionContext

  val session: Session

  def receive: Dialog.Receive

  def serialize(): JsObject = JsObject()

  def deserialize(obj: JsValue): Unit = ()

}
