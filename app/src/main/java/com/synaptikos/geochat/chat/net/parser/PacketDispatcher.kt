package com.synaptikos.geochat.chat.net.parser

import com.synaptikos.geochat.chat.net.Packet
import com.synaptikos.geochat.chat.net.handler.PacketHandler
import java.lang.reflect.Method
import java.nio.ByteBuffer

class PacketDispatcher {
  private val parserMap = mutableMapOf<Byte, PacketParser<*>>()
  private val handlerMap = mutableMapOf<Byte, MutableList<HandlerData>>()

  fun registerParser(parser: PacketParser<*>) {
    this.parserMap.put(parser.packetId, parser)
  }

  fun registerHandlers(handler: Any) {
    for (m in handler.javaClass.methods) {
      if (!m.isAnnotationPresent(PacketHandler::class.java))
        continue
      val id = m.getAnnotation(PacketHandler::class.java).packetId
      if (!this.handlerMap.containsKey(id))
        this.handlerMap.put(id, mutableListOf<HandlerData>())
      this.handlerMap[id]!!.add(HandlerData(handler, m))
    }
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
      it.method.invoke(it.receiver, packet)
    }
  }

  data class HandlerData(val receiver: Any, val method: Method)
}