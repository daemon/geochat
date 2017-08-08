package com.synaptikos.geochat.chat.net.handler

import com.synaptikos.geochat.chat.net.Packet

abstract class PacketHandler(val packetId: Byte) {
  abstract fun handle(packet: Packet)
}