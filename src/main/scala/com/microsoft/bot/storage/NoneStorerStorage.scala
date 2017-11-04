package com.microsoft.bot.storage

import com.microsoft.bot.models.{BotData, ErrorResponse}
import spray.json.JsArray

case class NoneStorerStorage() extends BotStorage {
  def getUserData(channelId: String, userId: String): Either[ErrorResponse, BotData] = {
    Right(get())
  }

  def setUserData(channelId: String, userId: String, data: BotData): Either[ErrorResponse, BotData] = {
    Right(set(data))
  }

  def deleteUserData(channelId: String, userId: String): Either[ErrorResponse, BotData] = {
    Right(delete())
  }

  def getConversationData(channelId: String, conversationId: String): Either[ErrorResponse, BotData] = {
    Right(get())
  }

  def setConversationData(channelId: String, conversationId: String, data: BotData): Either[ErrorResponse, BotData] = {
    Right(set(data))
  }

  def deleteConversationData(channelId: String, conversationId: String): Either[ErrorResponse, BotData] = {
    Right(delete())
  }

  def getPrivateConversationData(channelId: String, conversationId: String, userId: String): Either[ErrorResponse, BotData] = {
    Right(get())
  }

  def setPrivateConversationData(channelId: String, conversationId: String, userId: String, data: BotData): Either[ErrorResponse, BotData] = {
    Right(set(data))
  }

  def deletePrivateConversationData(channelId: String, conversationId: String, userId: String): Either[ErrorResponse, BotData] = {
    Right(delete())
  }

  private def get() = BotData("*", Option(JsArray()))
  private def set(data: BotData) = data
  private def delete() = BotData("*", Option(JsArray()))

}
