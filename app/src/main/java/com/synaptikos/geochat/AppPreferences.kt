package com.synaptikos.geochat

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class AppPreferences(val context: Context) {
  private val preferences = PreferenceManager.getDefaultSharedPreferences(this.context)
  var username: String
    get() = this.preferences.getString("username", "")
    set(username) = this.preferences.wrapApply({it.putString("username", username)})
  var password: String
    get() = this.preferences.getString("password", "")
    set(password) = this.preferences.wrapApply({it.putString("password", password)})
  var authToken: String
    get() = this.preferences.getString("authToken", "")
    set(authToken) = this.preferences.wrapApply({it.putString("authToken", authToken)})

  fun SharedPreferences.wrapApply(call: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    call.invoke(editor)
    editor.apply()
  }
}