package com.synaptikos.geochat.chat.net

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.synaptikos.geochat.AppPreferences
import com.synaptikos.geochat.R
import com.synaptikos.geochat.api.ApiClient
import com.synaptikos.geochat.api.ApiResponse
import com.synaptikos.geochat.api.user.AuthResponse
import com.synaptikos.geochat.api.user.LoginUserData
import com.synaptikos.geochat.api.user.UserService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BackgroundChatService : IntentService("bg-chat-service") {
  override fun onHandleIntent(intent: Intent?) {
    val preferences = AppPreferences(this)
    if (preferences.authToken == "")
      return
    val client = ApiClient.getService<UserService>(this)
    client.loginUser(LoginUserData(token=preferences.authToken)).enqueue(object : Callback<ResponseBody> {
      override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
        val rc: AuthResponse = ApiResponse.toResponse(response?.body())
        if (rc.isSuccess()) {
          
        }
      }

      override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {}
    })
    val connection = ChatConnection(this.getString(R.string.serverUrl))
    preferences.authToken
  }

  class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

    }
  }
}