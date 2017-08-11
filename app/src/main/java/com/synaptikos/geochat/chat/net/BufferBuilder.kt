package com.synaptikos.geochat.chat.net

import java.nio.ByteBuffer

class BufferBuilder {
  private val objects = mutableListOf<Any>()

  fun writeString(data: String): BufferBuilder {
    this.objects.add(data)
    return this
  }

  fun writeHeader(header: Packet.Header): BufferBuilder {
    this.objects.add(header)
    return this
  }

  fun build(): ByteBuffer {
    return writeAll(this.objects.toTypedArray())
  }

  companion object {
    fun writeAll(vararg objects: Any): ByteBuffer {
      val buffer = ByteBuffer.allocate(Packet.computeLength(objects))
      for (o in objects) {
        when (o) {
          is String -> {
            val array = o.toByteArray()
            buffer.putInt(array.size)
            buffer.put(array)
          }
          is Int -> buffer.putInt(o)
          is Long -> buffer.putLong(o)
          is ByteArray -> buffer.put(o)
          is Packet.Header -> {
            buffer.put(o.id)
            buffer.putInt(o.length)
          }
        }
      }
      return buffer
    }
  }
}