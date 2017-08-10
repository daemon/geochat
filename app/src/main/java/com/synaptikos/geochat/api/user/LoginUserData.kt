package com.synaptikos.geochat.api.user

data class LoginUserData(val user: String="", val password: String="", val token: String="", val captcha_token: String="")