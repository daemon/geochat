package com.synaptikos.geochat.chat.net

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.support.v7.app.NotificationCompat

class MessageNotifier(val context: Context, val message: S2CChatMessage) {
  companion object {
    const val NOTIFICATION_ID = 1
  }

  private val content: String
  private val manager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    val builder = StringBuilder()
    val m = message.message.substring(0, minOf(message.message.length, 60))
    builder.append(message.username).append(": ").append(m)
    if (m.length != message.message.length)
      builder.append("...")
    this.content = builder.toString()
  }

  // TODO open chat on press
  fun open() {
    val builder = NotificationCompat.Builder(this.context).setContentTitle("New GeoMessage")
        .setContentText(this.content).setOnlyAlertOnce(true).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .setVibrate(LongArray(1){500})
    this.manager.notify(NOTIFICATION_ID, builder.build())
  }
}