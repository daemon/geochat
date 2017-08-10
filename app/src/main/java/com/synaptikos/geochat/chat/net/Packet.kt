package com.synaptikos.geochat.chat.net

import java.nio.ByteBuffer

abstract class Packet(val header: Header) {
  val id = this.header.id
  val length = this.header.length
  companion object {
    const val S2C_TEXT_MESSAGE: Byte = 0x01
    const val C2S_TEXT_MESSAGE: Byte = 0x01

    fun computeLength(vararg objects: Any): Int {
      var length = 0
      for (o in objects) {
        when (o) {
          is Int -> length += 4
          is String -> length += o.toByteArray().size + 4
          is Long -> length += 8
        }
      }
      return length
    }
  }

  abstract fun buffer(): ByteBuffer

  data class Header(val id: Byte, val length: Int)
}
