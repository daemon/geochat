package com.synaptikos.geochat.chat.net

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import com.synaptikos.geochat.AppPreferences
import com.synaptikos.geochat.R
import com.synaptikos.geochat.api.ApiClient
import com.synaptikos.geochat.api.ApiResponse
import com.synaptikos.geochat.api.user.AuthResponse
import com.synaptikos.geochat.api.user.LoginUserData
import com.synaptikos.geochat.api.user.UserService
import com.synaptikos.geochat.chat.net.handler.PacketHandler
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BackgroundChatService : IntentService("bg-chat-service") {
  companion object {
    val TEXT_MESSAGE_BROADCAST = "bg-chat-service-txt-broadcast"
    private var running = false
    private var inForeground = false
    fun start(context: Context) {
      synchronized(running) {
        if (running)
          return
        context.startService(Intent(context, BackgroundChatService::class.java))
      }
    }
  }
  private lateinit var connection: ChatConnection

  override fun onHandleIntent(intent: Intent?) {
    val preferences = AppPreferences(this)
    if (preferences.authToken == "")
      return
    val client = ApiClient.getService<UserService>(this)
    client.loginUser(LoginUserData(token=preferences.authToken)).enqueue(object : Callback<ResponseBody> {
      override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
        val rc: AuthResponse = ApiResponse.toResponse(response?.body())
        if (rc.isSuccess())
          beginChatConnection()
      }
      override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {}
    })
  }

  private fun beginChatConnection() {
    running = true
    this.connection = ChatConnection(this.getString(R.string.serverUrl))
    this.connection.registerHandlers(this)
  }

  @PacketHandler(Packet.S2C_TEXT_MESSAGE)
  private fun onTextMessage(message: S2CChatMessage) {
    val intent = Intent(TEXT_MESSAGE_BROADCAST).putExtra("message", message.message)
        .putExtra("name", message.username).putExtra("ts", message.ts)
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    if (!inForeground)
      MessageNotifier(this, message).open()
  }

  class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action != "android.intent.action.BOOT_COMPLETED")
        return
      if (context != null)
        start(context)
    }
  }
}