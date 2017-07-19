package com.synaptikos.geochat.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.ResponseBody

open class ApiResponse(val field: Int) {
  private var responseCodes: List<ResponseCode> = ArrayList()

  constructor(field: Int, vararg responseCodes: ResponseCode) : this(field) {
    this.responseCodes = responseCodes.toList()
  }

  companion object {
    inline fun <reified T> toResponse(body: ResponseBody?): T {
      val jsonElement = JsonParser().parse(body?.string())
      val field = jsonElement.asJsonObject.get("field").asInt
      val ctor = T::class.java.getConstructor(Int::class.java)
      return ctor.newInstance(field)
    }
  }

  fun isSet(code: ResponseCode): Boolean = code.isFlipped(this.field)

  fun getAllResponseCodes(): List<ResponseCode> = this.responseCodes.filter({r -> r.isFlipped(this.field)})
}