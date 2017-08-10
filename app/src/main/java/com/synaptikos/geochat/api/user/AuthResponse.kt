package com.synaptikos.geochat.api.user

import com.synaptikos.geochat.R
import com.synaptikos.geochat.api.ApiResponse
import com.synaptikos.geochat.api.ResponseCode

class AuthResponse : ApiResponse {
  constructor(field: Int) : super(field, USERNAME_TAKEN, PASSWORD_INSECURE, EMAIL_TAKEN, CAPTCHA_EXPIRED, TOKEN_EXPIRED)

  fun authToken(): String {
    return this.jsonBody?.get("auth_token")?.asString ?: ""
  }

  companion object {
    val USERNAME_TAKEN = ResponseCode(1, R.string.authCode2)
    val PASSWORD_INSECURE = ResponseCode(2, R.string.authCode3)
    val EMAIL_TAKEN = ResponseCode(4, R.string.authCode4)
    val CAPTCHA_EXPIRED = ResponseCode(8, R.string.authCode8)
    val TOKEN_EXPIRED = ResponseCode(16, R.string.authCode16)
  }
}