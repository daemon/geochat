package com.synaptikos.geochat.api.auth

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
  @POST("/user")
  fun createUser(@Body user: RegisterUserData): Call<AuthResponse>
}