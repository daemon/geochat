package com.synaptikos.geochat.api.auth

data class AuthResponse(val code: Int) {
  private val SUCCESS = 0
  private val USERNAME_TAKEN = 1
  private val PASSWORD_INSECURE = 2
  private val EMAIL_TAKEN = 4

  fun isSuccess(): Boolean {
    return this.code == SUCCESS
  }

  fun isUsernameTaken(): Boolean {
    return (this.code and USERNAME_TAKEN) != 0
  }

  fun isPasswordInsecure(): Boolean {
    return (this.code and PASSWORD_INSECURE) != 0
  }

  fun isEmailTaken(): Boolean {
    return (this.code and EMAIL_TAKEN) != 0
  }
}