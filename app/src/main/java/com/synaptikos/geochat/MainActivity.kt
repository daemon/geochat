package com.synaptikos.geochat

import android.content.Intent
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.safetynet.SafetyNet
import com.synaptikos.geochat.api.ApiClient
import com.synaptikos.geochat.api.ApiResponse
import com.synaptikos.geochat.api.user.AuthResponse
import com.synaptikos.geochat.api.user.UserService
import com.synaptikos.geochat.api.user.RegisterUserData
import com.synaptikos.geochat.gui.KeyboardCloser
import com.synaptikos.geochat.gui.MessageBox
import com.tomergoldst.tooltips.ToolTip
import com.tomergoldst.tooltips.ToolTipsManager
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
  enum class FormState {
    REGISTER, LOGIN
  }
  private var state = FormState.LOGIN
  private var lastLength = 0
  private val ttManager = ToolTipsManager()
  private lateinit var userService: UserService
  private lateinit var apiClient: GoogleApiClient
  private var captchaToken: String? = null
  private lateinit var preferences: AppPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.preferences = AppPreferences(this)
    setContentView(R.layout.activity_main)
    this.setupServices()
    this.setupToolTips()
    this.setupPasswordFont()
    this.setupKeyboardListener(this.authRoot)
    this.setupTabListeners()
    this.setupInputFields()
    this.setupSubmitListener()
  }

  private fun setupInputFields() {
    this.passwordEditText.setOnKeyListener(fun (_, _, _): Boolean {
      val length = this.passwordEditText.text.length
      if (length >= 8 && this.lastLength < 8)
        this.ttManager.findAndDismiss(this.passwordEditText)
      if (length < 8 && this.lastLength >= 8)
        this.showPasswordToolTip()
      this.lastLength = length
      return false
    })
    this.passwordConfirmEditText.setOnKeyListener(fun(_, _, _): Boolean {
      if (this.passwordConfirmEditText.text.toString() == this.passwordEditText.text.toString() &&
          this.passwordEditText.length() > 0)
        this.ttManager.findAndDismiss(this.passwordConfirmEditText)
      return false
    })
  }

  private fun setupServices() {
    this.userService = ApiClient.getService(this)
    this.apiClient = GoogleApiClient.Builder(this).addApi(SafetyNet.API)
        .addOnConnectionFailedListener({ c -> println(c.toString())}).build()
    this.apiClient.connect()
  }

  private fun doSubmit() {
    if (this.captchaToken == null)
      return
    if (this.state == FormState.REGISTER)
      this.userService.createUser(RegisterUserData(this.emailEditText.text.toString(),
          this.usernameEditText.text.toString(), this.passwordEditText.text.toString(), this.captchaToken!!)).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
          if (response == null)
            return
          val rc: AuthResponse = ApiResponse.toResponse(response.body())
          if (rc.isSuccess()) {
            preferences.authToken = rc.authToken()
            preferences.password = passwordEditText.text.toString()
            preferences.username = usernameEditText.text.toString()
            startActivity(Intent(this@MainActivity, ChatActivity::class.java))
            return
          }
          val builder = StringBuilder()
          for (r in rc.getAllResponseCodes())
            builder.append(r.toString(this@MainActivity)).append("\n")
          MessageBox(this@MainActivity, getString(R.string.error), builder.toString()).show()
        }
        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) = showSubmitButton()
      })
  }

  private fun setupSubmitListener() {
    this.authSubmit.setOnClickListener(fun (_) {
      if (this.captchaToken != null) {
        this.doSubmit()
        return
      }
      val siteKey: String = this.getString(R.string.apiKey)
      SafetyNet.SafetyNetApi.verifyWithRecaptcha(this.apiClient, siteKey).setResultCallback(fun (r) {
        val status = r.status
        if (!status.isSuccess())
          return
        this.hideSubmitButton()
        this.captchaToken = r.tokenResult
        this.doSubmit()
      })
    })
  }

  override fun onBackPressed() {
    if (this.authSubmit.visibility != View.INVISIBLE)
      super.onBackPressed()
    else
      this.showSubmitButton()
  }

  private fun showPasswordToolTip() {
    if (this.passwordEditText.length() >= 8)
      return
    val pwReqBuilder = ToolTip.Builder(this, passwordEditText, authFormRoot,
        this.resources.getString(R.string.passwordRequirementTooltip), ToolTip.POSITION_ABOVE)
    pwReqBuilder.setAlign(ToolTip.ALIGN_CENTER)
    this.ttManager.show(pwReqBuilder.build())
  }

  private fun showPasswordConfirmToolTip() {
    if (this.passwordEditText.text.toString() == this.passwordConfirmEditText.text.toString() &&
        this.passwordEditText.length() > 0)
      return
    val pwConfirmBuilder = ToolTip.Builder(this, passwordConfirmEditText, authFormRoot,
        this.resources.getString(R.string.passwordConfirmTooltip), ToolTip.POSITION_ABOVE)
    pwConfirmBuilder.setAlign(ToolTip.ALIGN_CENTER)
    this.ttManager.show(pwConfirmBuilder.build())
  }

  private fun setupToolTips() {
    this.passwordEditText.setOnFocusChangeListener(fun (view, onFocus) {
      if (onFocus && this.state == FormState.REGISTER)
        this.showPasswordToolTip()
      else
        this.ttManager.findAndDismiss(this.passwordEditText)
    })
    this.passwordConfirmEditText.setOnFocusChangeListener(fun (view, onFocus) {
      if (onFocus && this.state == FormState.REGISTER)
        this.showPasswordConfirmToolTip()
      else
        this.ttManager.findAndDismiss(this.passwordConfirmEditText)
    })
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
    view.setOnTouchListener(fun (_, _): Boolean {
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
      this.usernameEditText.visibility = View.VISIBLE
      this.state = FormState.REGISTER
    }
  }

  private fun deflateRegister() {
    synchronized (this.authTabLayout) {
      this.passwordConfirmEditText.visibility = View.GONE
      this.usernameEditText.visibility = View.GONE
      this.ttManager.findAndDismiss(passwordEditText)
      this.state = FormState.LOGIN
    }
  }
}
