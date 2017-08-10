package com.synaptikos.geochat.chat.net

import com.synaptikos.geochat.chat.net.parser.PacketParser
import java.nio.ByteBuffer

class S2CChatMessage(val username: String, val message: String, val ts: Long) :
    Packet(Header(S2C_TEXT_MESSAGE, computeLength(username, message, ts))) {
  override fun buffer(): ByteBuffer {
    val buf = ByteBuffer.allocate(5 + this.length)
    buf.put(this.id).putInt(this.length).put(username.toByteArray()).put(message.toByteArray()).putLong(ts)
    return buf
  }

  companion object {
    fun createParser(): PacketParser<S2CChatMessage> {
      return object : PacketParser<S2CChatMessage>(S2C_TEXT_MESSAGE) {
        override fun parsePacket(buffer: ByteBuffer): S2CChatMessage {
          val reader = BufferReader(buffer)
          val header = reader.readPacketHeader()
          return S2CChatMessage(reader.readString(), reader.readString(), buffer.getLong())
        }
      }
    }
  }
}