package com.synaptikos.geochat.api.user

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {
  @POST("/user")
  fun createUser(@Body user: RegisterUserData): Call<ResponseBody>
  @GET("/user")
  fun loginUser(@Body user: LoginUserData): Call<ResponseBody>
  @POST("/user/location")
  fun setLocation(@Body location: LocationUserData): Call<ResponseBody>
}