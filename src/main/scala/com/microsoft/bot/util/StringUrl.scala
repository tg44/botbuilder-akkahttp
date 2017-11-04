package com.microsoft.bot.util

object StringUrl {

  def urlJoin(start: String, end: String): String = {
    if (start.endsWith("/")) {
      if (end.startsWith("/")) {
        start + end.tail
      } else {
        start + end
      }
    } else {
      if (end.startsWith("/")) {
        start + end
      } else {
        start + "/" + end
      }
    }
  }

}
