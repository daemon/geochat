package com.synaptikos.geochat.chat.net

import java.nio.ByteBuffer

abstract class Packet(val id: Byte, val length: Int) {
  companion object {
    val S2C_TEXT_MESSAGE = 0x01
    val C2S_TEXT_MESSAGE = 0x01
  }

  abstract fun buffer(): ByteBuffer
}
