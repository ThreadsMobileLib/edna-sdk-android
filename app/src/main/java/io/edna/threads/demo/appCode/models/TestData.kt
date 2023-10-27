package io.edna.threads.demo.appCode.models

import com.google.gson.Gson

data class TestData(
    val userInfo: UserInfo? = null,
    val serverConfig: ServerConfig? = null
) {
    fun toJson() = Gson().toJson(this)

    companion object {
        fun fromJson(json: String) = Gson().fromJson(json, TestData::class.java)
    }
}
