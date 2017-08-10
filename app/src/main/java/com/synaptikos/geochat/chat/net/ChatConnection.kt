package com.synaptikos.geochat.chat.net

import com.synaptikos.geochat.chat.net.handler.PacketHandler
import com.synaptikos.geochat.chat.net.parser.InsufficientDataException
import com.synaptikos.geochat.chat.net.parser.PacketDispatcher
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock

class ChatConnection(val host: String, val port: Int = 8193) {
  private lateinit var channel: SocketChannel
  private var sendThread: Thread
  private var running = true
  private var receiving = false
  private var sending = false
  private val ioLock = ReentrantLock()
  private val notReceiving = this.ioLock.newCondition()
  private val notSending = this.ioLock.newCondition()
  private val connected = this.ioLock.newCondition()
  private val sendLock = ReentrantLock()
  private val notQueueEmpty = this.sendLock.newCondition()
  private val sendQueue = ConcurrentLinkedQueue<Packet>()
  private val dispatcher = PacketDispatcher()

  init {
    this.setupDispatcher()
    Thread {
      var reconnectDelay = 100L
      while (this.running) {
        this.ioLock.lock()
        try {
          while (this.receiving)
            this.notReceiving.await()
          while (this.sending)
            this.notSending.await()
          if (this.forceReconnect())
            reconnectDelay = 100L
          else
            this.connected.signalAll()
        } finally {
          this.ioLock.unlock()
        }
        Thread.sleep(reconnectDelay)
        reconnectDelay = maxOf(2 * reconnectDelay, 60000 * 5L)
      }
    }.start()
    Thread {
      while (this.running) {
        this.ioLock.lock()
        while (!this.channel.isConnected)
          this.connected.await()
        this.ioLock.unlock()
        this.readLoop()
      }
    }.start()
    this.sendThread = Thread {
      while (this.running) {
        this.ioLock.lock()
        while (!this.channel.isConnected)
          this.connected.await()
        this.ioLock.unlock()
        this.sendLoop()
      }
    }
    this.sendThread.start()
  }

  fun sendMessage(message: String) {
    this.sendPacket(C2SChatMessage(message))
  }

  fun registerHandlers(handler: Any) {
    this.dispatcher.registerHandlers(handler)
  }

  private fun setupDispatcher() {
    this.dispatcher.registerParser(S2CChatMessage.createParser())
    this.dispatcher.registerHandlers(this)
  }

  private fun sendPacket(packet: Packet) {
    this.sendLock.lock()
    try {
      this.sendQueue.add(packet)
      this.notQueueEmpty.signal()
    } finally {
      this.sendLock.unlock()
    }
  }

  private fun forceReconnect(): Boolean {
    this.channel = SocketChannel.open()
    try {
      this.channel.connect(InetSocketAddress(this.host, this.port))
    } catch(e: Throwable) {
      return false
    }
    this.receiving = true
    this.sending = true
    return true
  }

  private fun readLoop() {
    var buffer = ByteBuffer.allocate(4096)
    try {
      do {
        val read = this.channel.read(buffer)
        buffer.flip()
        try {
          while (buffer.hasRemaining())
            this.dispatcher.dispatch(buffer)
          buffer.reset()
        } catch(e: InsufficientDataException) {
          buffer = ByteBuffer.allocate(4096).put(buffer)
        }
      } while (read > 0)
    } catch (e: Throwable) {}
    this.channel.close()
  }

  private fun sendLoop() {
    this.sendLock.lock()
    while (this.sendQueue.size == 0)
      this.notQueueEmpty.await()
    this.sendLock.unlock()
    while (this.sendQueue.size > 0) {
      try {
        this.channel.write(this.sendQueue.peek().buffer())
        this.sendQueue.poll()
      } catch (e: Throwable) {
        return
      }
    }
  }
}