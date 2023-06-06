package io.edna.threads.demo.appCode.adapters.serverList

import io.edna.threads.demo.appCode.models.ServerConfig

interface ServerListItemOnClickListener {
    fun onClick(item: ServerConfig)
    fun onEditItem(item: ServerConfig)
    fun onRemoveItem(item: ServerConfig)
}
