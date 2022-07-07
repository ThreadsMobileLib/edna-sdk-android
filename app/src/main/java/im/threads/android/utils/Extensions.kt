package im.threads.android.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun Any.toJson(): String = Gson().toJson(this, object : TypeToken<Any>() {}.type)

inline fun <reified T> Gson.fromJson(json: String) = fromJson<T>(json, object : TypeToken<T>() {}.type)
