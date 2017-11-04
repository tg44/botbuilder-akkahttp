package com.microsoft.bot.util

import java.math.BigInteger
import java.security.{KeyFactory, PublicKey}

import scala.util.Try

object CryptoHelper {
  //http://www.javased.com/?api=java.security.spec.RSAPublicKeySpec

  val keyFactory = KeyFactory.getInstance("RSA")
  protected val SPLIT = '#'

  def getPem(n: String, e: String): PublicKey = {
    import java.security.spec.RSAPublicKeySpec
    val modulus = base64ToBigInt(n)
    val exponent = base64ToBigInt(e)
    val publicKeySpec = new RSAPublicKeySpec(modulus, exponent)
    keyFactory.generatePublic(publicKeySpec)
  }

  def keyToString(key: PublicKey): Try[String] = {
    import java.security.spec.RSAPublicKeySpec
    Try {
      val spec = keyFactory.getKeySpec(key, classOf[RSAPublicKeySpec])
      val buf = new StringBuilder
      buf.append(spec.getModulus.toString(16)).append(SPLIT).append(spec.getPublicExponent.toString(16))
      buf.toString
    }
  }

  def base64ToBigInt(base64Str: String): BigInteger = {
    import java.math.BigInteger
    import java.util.Base64

    val bytes = Base64.getUrlDecoder.decode(base64Str)
    new BigInteger(1, bytes)
  }
}
