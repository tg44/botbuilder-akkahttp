package com.microsoft.bot.models

import spray.json.{JsObject, JsValue}

//todo lenses
case class Activity(
    header: ActivityHeader = ActivityHeader(),
    actions: ActionAttributes = ActionAttributes(),
    channelSettings: ChannelSettingsInfo = ChannelSettingsInfo(),
    content: Content = Content()
) {

  def createReplyHeader(msgType: Option[String] = Option(Activity.MESSAGE_TYPE)): ActivityHeader = {
    //todo timeStamp and timehandling long -> string and string -> long
    ActivityHeader(
      `type` = msgType,
      from = header.recipient, // this is a case class, totally immutable, no need to copy
      recipient = header.from,
      replyToId = header.replyToId,
      serviceUrl = header.serviceUrl,
      channelId = header.channelId,
      conversation = header.conversation
    )
  }

  def createReply(text: Option[String] = None, locale: Option[String] = None): Activity = {
    Activity(createReplyHeader(), ActionAttributes(), ChannelSettingsInfo(locale = locale), Content(text = text))
  }

  def isEmulator: Boolean = header.channelId.getOrElse("") == "emulator"

  def hasContent: Boolean = {
    (!content.text.getOrElse("").isEmpty) ||
    (!content.summary.getOrElse("").isEmpty) ||
    (content.attachments.getOrElse(Seq()).nonEmpty) ||
    (content.channelData.isDefined)
  }

  def getText: String = {
    content.text.getOrElse("")
  }
  /*
  def mentions(): Seq[Mention] = {
    content.entities.getOrElse(Seq())
      .filter(ent => ent.`type`.getOrElse("").toLowerCase() == "mention")
      .map(ent => ent.properties.getOrElse(new JsObject(Map())).convertTo[Mention])
  }*/
}

object Activity {
  //todo enum?
  val MESSAGE_TYPE = "message"
  val CONTACT_RELATION_UPDATE = "contactRelationUpdate"
  val CONVERSATION_UPDATE = "conversationUpdate"
  val TYPING = "typing"
  val PING = "ping"
  val END_OF_CONVERSATION = "endOfConversation"
  val TRIGGER = "trigger"
  val EVENT = "event"
  val INVOKE = "invoke"
  val DELETE_USER_DATA = "deleteUserData"
  val INSTALLATION_UPDATE = "installationUpdate"
}

case class ActivityHeader(
    /* The type of the activity [message|contactRelationUpdate|converationUpdate|typing|endOfConversation|event|invoke] */
    `type`: Option[String] = None,
    /* ID of this activity */
    id: Option[String] = None,
    /* UTC Time when message was sent (set by service) */
    timestamp: Option[String] = None,
    /* Local time when message was sent (set by client, Ex: 2016-09-23T13:07:49.4714686-07:00) */
    localTimestamp: Option[String] = None,
    /* Service endpoint where operations concerning the activity may be performed */
    serviceUrl: Option[String] = None,
    /* ID of the channel where the activity was sent */
    channelId: Option[String] = None,
    from: Option[Bot] = None,
    conversation: Option[ActivityConversation] = None,
    recipient: Option[Bot] = None,
    /* The original ID this message is a response to */
    replyToId: Option[String] = None,
    value: Option[JsValue] = None,
    /* Name of the operation to invoke or the name of the event */
    name: Option[String] = None,
    relatesTo: Option[ActivityRelatesTo] = None,
    /* Code indicating why the conversation has ended */
    code: Option[String] = None
)
case class ActionAttributes(
    /* Members added to the conversation */
    membersAdded: Option[Seq[Bot]] = None,
    /* Members removed from the conversation */
    membersRemoved: Option[Seq[Bot]] = None,
    /* Reactions added to the activity */
    reactionsAdded: Option[Seq[ActivityReactionsAdded]] = None,
    /* Reactions removed from the activity */
    reactionsRemoved: Option[Seq[ActivityReactionsAdded]] = None,
    /* The conversation's updated topic name */
    topicName: Option[String] = None,
    /* ContactAdded/Removed action */
    action: Option[String] = None
)
case class ChannelSettingsInfo(
    /* Format of text fields [plain|markdown] Default:markdown */
    textFormat: Option[String] = None,
    /* Hint for how to deal with multiple attachments: [list|carousel] Default:list */
    attachmentLayout: Option[String] = None,
    /* True if prior history of the channel is disclosed */
    historyDisclosed: Option[Boolean] = None,
    /* The language code of the Text field */
    locale: Option[String] = None,
    /* Indicates whether the bot is accepting, expecting, or ignoring input */
    inputHint: Option[String] = None
)
case class Content(
    /* Content for the message */
    text: Option[String] = None,
    /* SSML Speak for TTS audio response */
    speak: Option[String] = None,
    /* Text to display if the channel cannot render cards */
    summary: Option[String] = None,
    /* Attachments */
    attachments: Option[Seq[ActivityAttachments]] = None,
    suggestedActions: Option[ActivitySuggestedActions] = None,
    /* Collection of Entity objects, each of which contains metadata about this activity. Each Entity object is typed. */
    entities: Option[Seq[ActivityEntities]] = None,
    channelData: Option[JsValue] = None
)

////////

case class ActivityAttachments(
    /* mimetype/Contenttype for the file */
    contentType: Option[String] = None,
    /* Content Url */
    contentUrl: Option[String] = None,
    content: Option[JsValue] = None,
    /* (OPTIONAL) The name of the attachment */
    name: Option[String] = None,
    /* (OPTIONAL) Thumbnail associated with attachment */
    thumbnailUrl: Option[String] = None
)

case class ActivityConversation(
    /* Is this a reference to a group */
    isGroup: Option[Boolean],
    /* Channel id for the user or bot on this channel (Example: joe@smith.com, or @joesmith or 123456) */
    id: Option[String],
    /* Display friendly name */
    name: Option[String]
)

case class ActivityEntities(
    /* Entity Type (typically from schema.org types) */
    `type`: Option[String],
    properties: Option[JsObject]
)

case class ActivityReactionsAdded(
    /* Message reaction type */
    `type`: Option[String]
)

case class ActivityRelatesTo(
    /* (Optional) ID of the activity to refer to */
    activityId: Option[String],
    user: Option[Bot],
    bot: Option[Bot],
    conversation: Option[ActivityConversation],
    /* Channel ID */
    channelId: Option[String],
    /* Service endpoint where operations concerning the referenced conversation may be performed */
    serviceUrl: Option[String]
)

case class ActivitySuggestedActions(
    /* Ids of the recipients that the actions should be shown to.  These Ids are relative to the channelId and a subset of all recipients of the activity */
    to: Option[Seq[String]],
    /* Actions that can be shown to the user */
    actions: Option[Seq[ActivitySuggestedActionsActions]]
)
case class ActivitySuggestedActionsActions(
    /* The type of action implemented by this button */
    `type`: Option[String],
    /* Text description which appears on the button */
    title: Option[String],
    /* Image URL which will appear on the button, next to text label */
    image: Option[String],
    /* Text for this action */
    text: Option[String],
    /* (Optional) text to display in the chat feed if the button is clicked */
    displayText: Option[String],
    value: Option[JsValue]
)

case class Bot(
    /* Channel id for the user or bot on this channel (Example: joe@smith.com, or @joesmith or 123456) */
    id: Option[String],
    /* Display friendly name */
    name: Option[String]
)

case class Mention(
    mentioned: Option[Bot],
    text: Option[String]
)
