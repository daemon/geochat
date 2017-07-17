package com.synaptikos.geochat.gui

import android.content.Context
import android.support.v7.app.AlertDialog

class MessageBox(context: Context, title: String, message: String) {
  private val builder: AlertDialog.Builder
  init {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    builder.setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, { _, _ ->})
    this.builder = builder
  }

  fun show() = this.builder.show()
}