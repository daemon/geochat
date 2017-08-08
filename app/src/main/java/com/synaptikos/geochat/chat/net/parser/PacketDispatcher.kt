package com.synaptikos.geochat.chat.net.parser

import com.synaptikos.geochat.chat.net.Packet
import com.synaptikos.geochat.chat.net.handler.PacketHandler
import java.nio.ByteBuffer

class PacketDispatcher {
  private val parserMap = mutableMapOf<Byte, PacketParser<*>>()
  private val handlerMap = mutableMapOf<Byte, MutableList<PacketHandler>>()

  fun registerParser(dispatcher: PacketParser<*>) {
    this.parserMap.put(dispatcher.packetId, dispatcher)
  }

  fun registerHandler(handler: PacketHandler) {
    if (this.handlerMap[handler.packetId] == null)
      this.handlerMap.put(handler.packetId, mutableListOf<PacketHandler>())
    this.handlerMap[handler.packetId]?.add(handler)
  }

  fun dispatch(buffer: ByteBuffer) {
    if (buffer.remaining() == 0)
      return
    else if (buffer.remaining() < 5)
      throw InsufficientDataException()
    val id = buffer.get()
    val length = buffer.getInt()
    if (length > buffer.remaining()) {
      buffer.position(buffer.position() - 5)
      throw InsufficientDataException()
    }
    val parser = this.parserMap[id]
    if (parser == null) {
      buffer.position(buffer.position() + length)
      return
    }
    buffer.position(buffer.position() - 5)
    val packet = parser.parsePacket(buffer)
    this.handlerMap[id]?.forEach {
      it.handle(packet)
    }
  }
}