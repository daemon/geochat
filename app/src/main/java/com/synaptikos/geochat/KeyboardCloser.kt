package com.synaptikos.geochat

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

class KeyboardCloser(val activity: Activity) {
  fun run() {
    val manager: InputMethodManager = this.activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    val view: View? = this.activity.currentFocus
    manager.hideSoftInputFromWindow(view?.windowToken, 0)
    println(view)
  }
}