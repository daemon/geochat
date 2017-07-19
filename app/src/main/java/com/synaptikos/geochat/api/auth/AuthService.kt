package com.synaptikos.geochat.api.auth

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
  @POST("/user")
  fun createUser(@Body user: RegisterUserData): Call<ResponseBody>
}