package com.synaptikos.geochat.api

import android.content.Context

class ResponseCode(val code: Int, val androidStrId: Int) {
  fun toString(context: Context): String {
    return context.getString(this.androidStrId)
  }

  fun isFlipped(field: Int): Boolean {
    return (field and this.code) != 0
  }
}