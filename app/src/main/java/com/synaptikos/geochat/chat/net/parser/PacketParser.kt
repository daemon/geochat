package com.synaptikos.geochat.chat.net.parser

import com.synaptikos.geochat.chat.net.Packet
import com.synaptikos.geochat.chat.net.handler.PacketHandler
import java.nio.ByteBuffer

abstract class PacketParser<out T : Packet>(val packetId: Byte) {
  abstract fun parsePacket(buffer: ByteBuffer): T
}

class InsufficientDataException : Exception()