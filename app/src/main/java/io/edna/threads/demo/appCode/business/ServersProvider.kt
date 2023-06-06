package io.edna.threads.demo.appCode.business

import android.annotation.SuppressLint
import android.content.Context
import io.edna.threads.demo.appCode.models.ServerConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream

class ServersProvider(
    private val context: Context,
    private val preferences: PreferencesProvider
) {
    @SuppressLint("DiscouragedApi")
    fun readServersFromFile(): ArrayList<ServerConfig> {
        val inputStream: InputStream = context.assets.open("servers_config.json")
        val content = StringBuilder()
        val reader = BufferedReader(inputStream.reader())
        inputStream.use { stream ->
            kotlin.runCatching {
                var line = reader.readLine()
                while (line != null) {
                    content.append(line.trim())
                    line = reader.readLine()
                }
                stream.close()
            }
        }
        val jsonArray = JSONObject(content.toString()).getJSONArray("servers")
        val servers = ArrayList<ServerConfig>(jsonArray.length())
        for (i in 0 until jsonArray.length()) {
            val jsonObj = jsonArray.getJSONObject(i)
            servers.add(
                ServerConfig(
                    name = jsonObj.getString("name"),
                    threadsGateProviderUid = jsonObj.getString("threadsGateProviderUid"),
                    datastoreUrl = jsonObj.getString("datastoreUrl"),
                    serverBaseUrl = jsonObj.getString("serverBaseUrl"),
                    threadsGateUrl = jsonObj.getString("threadsGateUrl"),
                    trustedSSLCertificates = if (jsonObj.has("trustedSSLCertificates")) {
                        val certificates = ArrayList<Int>()
                        for (j in 0 until jsonObj.getJSONArray("trustedSSLCertificates").length()) {
                            val certName = jsonObj.getJSONArray("trustedSSLCertificates").get(j).toString()
                            val certId: Int = context.resources.getIdentifier(certName, "raw", context.packageName)
                            if (certId > 0) {
                                certificates.add(certId)
                            }
                        }
                        certificates
                    } else {
                        null
                    },
                    allowUntrustedSSLCertificate = if (jsonObj.has("allowUntrustedSSLCertificate")) {
                        jsonObj.getBoolean("allowUntrustedSSLCertificate")
                    } else {
                        false
                    }
                )
            )
        }
        return servers
    }

    fun saveServersToPreferences(servers: ArrayList<ServerConfig>) {
        preferences.saveServers(servers)
    }

    fun saveSelectedServer(server: ServerConfig) {
        preferences.saveSelectedServer(server)
    }

    fun getSelectedServer(): ServerConfig? {
        val selected = preferences.getSelectedServer()
        val servers = readServersFromFile()
        return if (selected != null) {
            servers.find { it.name == selected.name } ?: selected
        } else {
            if (servers.isNotEmpty()) {
                servers[0]
            } else {
                null
            }
        }
    }
}
