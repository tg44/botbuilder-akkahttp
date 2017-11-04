package com.microsoft.bot.bot

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.microsoft.bot.models._
import org.json4s.DefaultFormats
import spray.json.{DefaultJsonProtocol, JsObject, JsValue, JsonWriter, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val jsonFormats = DefaultFormats
  implicit def pimpedJsonObject(jsObj: JsObject) = new PimpedJsonObject(jsObj)

  implicit val botJsonFormatter: RootJsonFormat[Bot] = jsonFormat2(Bot)
  implicit val mentionJsonFormatter: RootJsonFormat[Mention] = jsonFormat2(Mention)
  implicit val activitySuggestedActionsActionsJsonFormatter: RootJsonFormat[ActivitySuggestedActionsActions] = jsonFormat6(ActivitySuggestedActionsActions)
  implicit val activitySuggestedActionsJsonFormatter: RootJsonFormat[ActivitySuggestedActions] = jsonFormat2(ActivitySuggestedActions)
  implicit val activityReactionsAddedJsonFormatter: RootJsonFormat[ActivityReactionsAdded] = jsonFormat1(ActivityReactionsAdded)
  implicit val activityEntitiesJsonFormatter: RootJsonFormat[ActivityEntities] = jsonFormat2(ActivityEntities)
  implicit val activityConversationJsonFormatter: RootJsonFormat[ActivityConversation] = jsonFormat3(ActivityConversation)
  implicit val activityAttachmentsJsonFormatter: RootJsonFormat[ActivityAttachments] = jsonFormat5(ActivityAttachments)
  implicit val activityRelatesToJsonFormatter: RootJsonFormat[ActivityRelatesTo] = jsonFormat6(ActivityRelatesTo)
  implicit val activityHeaderJsonFormatter: RootJsonFormat[ActivityHeader] = jsonFormat14(ActivityHeader)
  implicit val actionsJsonFormatter: RootJsonFormat[ActionAttributes] = jsonFormat6(ActionAttributes)
  implicit val channelSettingsJsonFormatter: RootJsonFormat[ChannelSettingsInfo] = jsonFormat5(ChannelSettingsInfo)
  implicit val contentJsonFormatter: RootJsonFormat[Content] = jsonFormat7(Content)

  implicit object ActivityJsonFormatter extends RootJsonFormat[Activity] {
    import spray.json._
    def write(a: Activity): JsObject = {
      val header = a.header.toJson.asJsObject
      val actions = a.actions.toJson.asJsObject
      val channelSettings = a.channelSettings.toJson.asJsObject
      val content = a.content.toJson.asJsObject
      header ++ actions ++ channelSettings ++ content
    }

    def read(value: JsValue): Activity = {
      val obj = value.asJsObject
      val header = obj.convertTo[ActivityHeader]
      val actions = obj.convertTo[ActionAttributes]
      val channelSettings = obj.convertTo[ChannelSettingsInfo]
      val content = obj.convertTo[Content]
      Activity(header, actions, channelSettings, content)
    }
  }

  class PimpedJsonObject(jsObj: JsObject) {
    def mergeWith(other: JsObject): JsObject = {
      new JsObject(jsObj.fields ++ other.fields)
    }

    def ++(other: JsObject): JsObject = jsObj mergeWith other
  }

}
