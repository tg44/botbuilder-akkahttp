package com.microsoft.bot.storage

import com.microsoft.bot.models.{BotData, ErrorResponse}

//todo monadic implementation

trait BotStorage {

  def getUserData(channelId: String, userId: String): Either[ErrorResponse, BotData]

  def setUserData(channelId: String, userId: String, data: BotData): Either[ErrorResponse, BotData]

  def deleteUserData(channelId: String, userId: String): Either[ErrorResponse, BotData]

  def getConversationData(channelId: String, conversationId: String): Either[ErrorResponse, BotData]

  def setConversationData(channelId: String, conversationId: String, data: BotData): Either[ErrorResponse, BotData]

  def deleteConversationData(channelId: String, conversationId: String): Either[ErrorResponse, BotData]

  def getPrivateConversationData(channelId: String, conversationId: String, userId: String): Either[ErrorResponse, BotData]

  def setPrivateConversationData(channelId: String, conversationId: String, userId: String, data: BotData): Either[ErrorResponse, BotData]

  def deletePrivateConversationData(channelId: String, conversationId: String, userId: String): Either[ErrorResponse, BotData]
}
