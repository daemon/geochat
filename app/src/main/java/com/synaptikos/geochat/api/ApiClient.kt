package com.synaptikos.geochat.api

import android.content.Context
import com.synaptikos.geochat.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
  fun getRetrofit(context: Context): Retrofit {
    return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
        .baseUrl(context.getString(R.string.baseUrl)).build()
  }

  inline fun <reified T> getService(context: Context): T {
    return getRetrofit(context).create(T::class.java)
  }
}