package com.synaptikos.geochat

import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
  enum class FormState {
    REGISTER, LOGIN
  }
  var state: FormState = FormState.LOGIN

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    this.setupPasswordFont()
    this.setupKeyboardListener(this.authRoot)
    this.setupTabListeners()
  }

  private fun setupTabListeners() {
    this.authTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.position) {
          0 -> deflateRegister()
          1 -> inflateRegister()
        }
      }
      override fun onTabUnselected(tab: TabLayout.Tab?) {}
      override fun onTabReselected(tab: TabLayout.Tab?) {}

    })
  }

  private fun setupPasswordFont() {
    this.passwordEditText.typeface = Typeface.DEFAULT
    this.passwordEditText.transformationMethod = PasswordTransformationMethod()
    this.passwordConfirmEditText.typeface = Typeface.DEFAULT
    this.passwordConfirmEditText.transformationMethod = PasswordTransformationMethod()
  }

  private fun setupKeyboardListener(view: View?) {
    if (view == null || view is EditText)
      return
    view.setOnTouchListener(fun (_, _: MotionEvent): Boolean {
      KeyboardCloser(this).run()
      return false
    })
    if (view is ViewGroup)
      for (i in 0 until view.childCount)
        this.setupKeyboardListener(view.getChildAt(i))
  }

  private fun showSubmitButton() {
    synchronized (this.authSubmitProgress) {
      this.authSubmitProgress.visibility = View.INVISIBLE
      this.authSubmit.visibility = View.VISIBLE
    }
  }

  private fun hideSubmitButton() {
    synchronized (this.authSubmitProgress) {
      this.authSubmitProgress.visibility = View.VISIBLE
      this.authSubmit.visibility = View.INVISIBLE
    }
  }

  private fun inflateRegister() {
    synchronized (this.authTabLayout) {
      this.passwordConfirmEditText.visibility = View.VISIBLE
      this.state = FormState.REGISTER
    }
  }

  private fun deflateRegister() {
    synchronized (this.authTabLayout) {
      this.passwordConfirmEditText.visibility = View.INVISIBLE
      this.state = FormState.LOGIN
    }
  }
}
