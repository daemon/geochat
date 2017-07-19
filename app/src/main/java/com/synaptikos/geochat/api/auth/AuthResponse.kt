package com.synaptikos.geochat.api.auth

import com.synaptikos.geochat.R
import com.synaptikos.geochat.api.ApiResponse
import com.synaptikos.geochat.api.ResponseCode
import java.util.*

class AuthResponse : ApiResponse {
  constructor(field: Int) : super(field, SUCCESS, USERNAME_TAKEN, PASSWORD_INSECURE, EMAIL_TAKEN)

  companion object {
    val SUCCESS = ResponseCode(0, R.string.authCode1)
    val USERNAME_TAKEN = ResponseCode(1, R.string.authCode2)
    val PASSWORD_INSECURE = ResponseCode(2, R.string.authCode3)
    val EMAIL_TAKEN = ResponseCode(4, R.string.authCode4)
  }
}