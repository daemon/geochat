package com.synaptikos.geochat.chat.net

import java.nio.ByteBuffer

class C2SChatMessage(val message: String) : Packet(Header(Packet.C2S_TEXT_MESSAGE, computeLength(message))) {
  override fun buffer(): ByteBuffer {
    return BufferBuilder.writeAll(this.header, this.message)
  }
}