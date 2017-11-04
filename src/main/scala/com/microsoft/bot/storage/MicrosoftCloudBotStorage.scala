package com.microsoft.bot.storage

import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.microsoft.bot.bot.{ChatConnectorSettings, SendTokenHelper}
import com.microsoft.bot.models.{BotData, BotDataJsonSupport, ErrorResponse, ErrorResponseJsonSupport}
import com.microsoft.bot.util.StringUrl
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class MicrosoftCloudBotStorage(
    settings: ChatConnectorSettings,
    tokenHelper: SendTokenHelper
)(implicit actorSystem: ActorSystem, executionContext: ExecutionContext, materializer: Materializer)
    extends BotDataJsonSupport
    with ErrorResponseJsonSupport
    with BotStorage {

  val requestWaitTimeout = 8.seconds
  val storageEndpoint = settings.stateEndpoint

  def userUrl(channelId: String, userId: String): String = {
    StringUrl.urlJoin(
      storageEndpoint,
      s"v3/botstate/${URLEncoder.encode(channelId, "UTF-8")}/users/${URLEncoder.encode(userId, "UTF-8")}"
    )
  }

  def conversationUrl(channelId: String, conversationId: String): String = {
    StringUrl.urlJoin(
      storageEndpoint,
      s"v3/botstate/${URLEncoder.encode(channelId, "UTF-8")}/conversations/${URLEncoder.encode(conversationId, "UTF-8")}"
    )
  }

  def privateConversationUrl(channelId: String, conversationId: String, userId: String): String = {
    StringUrl.urlJoin(
      storageEndpoint,
      s"v3/botstate/${URLEncoder.encode(channelId, "UTF-8")}/conversations/${URLEncoder.encode(conversationId, "UTF-8")}/users/${URLEncoder.encode(userId, "UTF-8")}"
    )
  }

  def getUserData(channelId: String, userId: String): Either[ErrorResponse, BotData] = {
    Await.result(get(userUrl(channelId, userId)), requestWaitTimeout)
  }

  def setUserData(channelId: String, userId: String, data: BotData): Either[ErrorResponse, BotData] = {
    Await.result(set(userUrl(channelId, userId), data), requestWaitTimeout)
  }

  def deleteUserData(channelId: String, userId: String): Either[ErrorResponse, BotData] = {
    Await.result(delete(userUrl(channelId, userId)), requestWaitTimeout)
  }

  def getConversationData(channelId: String, conversationId: String): Either[ErrorResponse, BotData] = {
    Await.result(get(conversationUrl(channelId, conversationId)), requestWaitTimeout)
  }

  def setConversationData(channelId: String, conversationId: String, data: BotData): Either[ErrorResponse, BotData] = {
    Await.result(set(conversationUrl(channelId, conversationId), data), requestWaitTimeout)
  }

  def deleteConversationData(channelId: String, conversationId: String): Either[ErrorResponse, BotData] = {
    Await.result(delete(conversationUrl(channelId, conversationId)), requestWaitTimeout)
  }

  def getPrivateConversationData(channelId: String, conversationId: String, userId: String): Either[ErrorResponse, BotData] = {
    Await.result(get(privateConversationUrl(channelId, conversationId, userId)), requestWaitTimeout)
  }

  def setPrivateConversationData(channelId: String, conversationId: String, userId: String, data: BotData): Either[ErrorResponse, BotData] = {
    println(channelId, conversationId, userId, data)
    Await.result(set(privateConversationUrl(channelId, conversationId, userId), data), requestWaitTimeout)
  }

  def deletePrivateConversationData(channelId: String, conversationId: String, userId: String): Either[ErrorResponse, BotData] = {
    Await.result(delete(privateConversationUrl(channelId, conversationId, userId)), requestWaitTimeout)
  }

  private def get(url: String): Future[Either[ErrorResponse, BotData]] = {
    tokenHelper.getHeaders.flatMap { headers =>
      val response = Http().singleRequest(
        HttpRequest(
          method = HttpMethods.GET,
          uri = url,
          headers = headers
        )
      )
      deserializeResponse(response)
    }
  }

  private def set(url: String, data: BotData): Future[Either[ErrorResponse, BotData]] = {
    tokenHelper.getHeaders.flatMap { headers =>
      val response = Http().singleRequest(
        HttpRequest(
          method = HttpMethods.POST,
          uri = url,
          headers = headers,
          entity = HttpEntity(ContentTypes.`application/json`, data.toJson.compactPrint)
        )
      )
      deserializeResponse(response)
    }
  }

  private def delete(url: String) = {
    tokenHelper.getHeaders.flatMap { headers =>
      val response = Http().singleRequest(
        HttpRequest(
          method = HttpMethods.DELETE,
          uri = url,
          headers = headers
        )
      )
      deserializeResponse(response)
    }
  }

  private def deserializeResponse(response: Future[HttpResponse]) = {
    response.flatMap { r =>
      println(r.status)
      if (r.status == StatusCodes.OK) {
        Unmarshal(r).to[BotData].map(Right(_))
      } else {
        Unmarshal(r).to[ErrorResponse].map(Left(_))
      }
    }
  }
}
