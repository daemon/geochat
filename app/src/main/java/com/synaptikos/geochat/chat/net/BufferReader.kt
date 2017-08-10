package com.synaptikos.geochat.chat.net

import java.nio.ByteBuffer
import java.nio.charset.Charset

class BufferReader(val buffer: ByteBuffer) {
  fun readString(): String {
    val length = this.buffer.getInt()
    val array = ByteArray(length)
    this.buffer.get(array)
    return String(array)
  }

  fun readPacketHeader(): Packet.Header {
    return Packet.Header(buffer.get(), buffer.getInt())
  }
}
