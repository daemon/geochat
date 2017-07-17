package com.synaptikos.geochat

import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.safetynet.SafetyNet
import com.synaptikos.geochat.api.ApiClient
import com.synaptikos.geochat.api.auth.AuthResponse
import com.synaptikos.geochat.api.auth.AuthService
import com.synaptikos.geochat.api.auth.RegisterUserData
import com.synaptikos.geochat.gui.KeyboardCloser
import com.tomergoldst.tooltips.ToolTip
import com.tomergoldst.tooltips.ToolTipsManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
  enum class FormState {
    REGISTER, LOGIN
  }
  private var state = FormState.LOGIN
  private val ttManager = ToolTipsManager()
  lateinit var authService: AuthService;
  lateinit var apiClient: GoogleApiClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    this.setupServices()
    this.setupToolTips()
    this.setupPasswordFont()
    this.setupKeyboardListener(this.authRoot)
    this.setupTabListeners()
    this.setupSubmitListener()
  }

  private fun setupServices() {
    this.authService = ApiClient.getService(this)
    this.apiClient = GoogleApiClient.Builder(this).addApi(SafetyNet.API)
        .addOnConnectionFailedListener({ c -> println(c.toString())}).build()
    this.apiClient.connect()
  }

  private fun setupSubmitListener() {
    this.authSubmit.setOnClickListener(fun (_) {
      val siteKey: String = this.getString(R.string.apiKey)
      SafetyNet.SafetyNetApi.verifyWithRecaptcha(this.apiClient, siteKey).setResultCallback(fun (r) {
        val status = r.status
        if (!status.isSuccess())
          return
        this.hideSubmitButton()
        val token = r.tokenResult
        if (this.state == FormState.LOGIN)
          this.authService.createUser(RegisterUserData(this.emailEditText.text.toString(),
              this.usernameEditText.text.toString(), this.passwordEditText.text.toString(), token)).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>?, response: Response<AuthResponse>?) {

            }
            override fun onFailure(call: Call<AuthResponse>?, t: Throwable?) = showSubmitButton()
          })
      })
    })
  }

  override fun onBackPressed() {
    if (this.authSubmit.visibility != View.INVISIBLE)
      super.onBackPressed()
    else
      this.showSubmitButton()
  }

  private fun setupToolTips() {
    val pwReqBuilder = ToolTip.Builder(this, passwordEditText, authFormRoot,
        this.resources.getString(R.string.passwordRequirementTooltip), ToolTip.POSITION_ABOVE)
    pwReqBuilder.setAlign(ToolTip.ALIGN_CENTER)
    val pwConfirmBuilder = ToolTip.Builder(this, passwordConfirmEditText, authFormRoot,
        this.resources.getString(R.string.passwordConfirmTooltip), ToolTip.POSITION_ABOVE)
    pwConfirmBuilder.setAlign(ToolTip.ALIGN_CENTER)
    this.passwordEditText.setOnFocusChangeListener(fun (view, onFocus) {
      if (onFocus && this.state == FormState.REGISTER)
        this.ttManager.show(pwReqBuilder.build())
      else
        this.ttManager.findAndDismiss(this.passwordEditText)
    })
    this.passwordConfirmEditText.setOnFocusChangeListener(fun (view, onFocus) {
      if (onFocus && this.state == FormState.REGISTER)
        this.ttManager.show(pwConfirmBuilder.build())
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
