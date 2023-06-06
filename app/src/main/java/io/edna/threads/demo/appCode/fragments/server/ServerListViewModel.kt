package io.edna.threads.demo.appCode.fragments.server

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import io.edna.threads.demo.BuildConfig
import io.edna.threads.demo.R
import io.edna.threads.demo.appCode.business.PreferencesProvider
import io.edna.threads.demo.appCode.business.ServersProvider
import io.edna.threads.demo.appCode.models.ServerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.parceler.Parcels

class ServerListViewModel(
    private val preferencesProvider: PreferencesProvider,
    private val serversProvider: ServersProvider
) : ViewModel(), DefaultLifecycleObserver {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var _serverListLiveData = MutableLiveData(preferencesProvider.getAllServers())
    var serverConfigLiveData: LiveData<ArrayList<ServerConfig>> = _serverListLiveData

    fun click(view: View) {
        val navigationController: NavController =
            (view.context as Activity).findNavController(R.id.nav_host_fragment_content_main)
        when (view.id) {
            R.id.backButton -> navigationController.navigate(R.id.action_ServersFragment_to_LaunchFragment)
            R.id.addServer -> {
                navigationController.navigate(R.id.action_ServerListFragment_to_AddServerFragment)
            }
        }
    }

    fun backToLaunchScreen(context: Context?) {
        if (context != null) {
            val navigationController: NavController =
                (context as Activity).findNavController(R.id.nav_host_fragment_content_main)
            navigationController.navigate(R.id.action_ServersFragment_to_LaunchFragment)
        }
    }

    private fun addConfig(config: ServerConfig) {
        coroutineScope.launch {
            addServer(config)
        }
    }

    fun removeConfig(config: ServerConfig) {
        coroutineScope.launch {
            removeServer(config)
        }
    }

    fun copyServersFromFileIfNeed() {
        if (preferencesProvider.getSavedAppVersion() != BuildConfig.VERSION_NAME) {
            coroutineScope.launch {
                copyServersFromFile()
            }
        } else {
            _serverListLiveData.postValue(preferencesProvider.getAllServers())
        }
    }

    fun callFragmentResultListener(key: String, bundle: Bundle) {
        if (key == ServerListFragment.SERVER_CONFIG_KEY && bundle.containsKey(ServerListFragment.SERVER_CONFIG_KEY)) {
            val config: ServerConfig? = if (Build.VERSION.SDK_INT >= 33) {
                Parcels.unwrap(
                    bundle.getParcelable(
                        ServerListFragment.SERVER_CONFIG_KEY,
                        Parcelable::class.java
                    )
                )
            } else {
                Parcels.unwrap(bundle.getParcelable(ServerListFragment.SERVER_CONFIG_KEY))
            }
            if (config != null) {
                addConfig(config)
            }
        }
    }

    private fun removeServer(config: ServerConfig) =
        coroutineScope.launch {
            val srcServers = preferencesProvider.getAllServers()
            val finalServers = removeServers(srcServers, config)
            _serverListLiveData.postValue(finalServers)
            preferencesProvider.saveServers(finalServers)
        }

    private fun addServer(config: ServerConfig) =
        coroutineScope.launch {
            val servers = preferencesProvider.getAllServers()
            val finalServers = updateServers(servers, config)
            _serverListLiveData.postValue(finalServers)
            preferencesProvider.saveServers(finalServers)
        }

    private fun copyServersFromFile() {
        val newServers = serversProvider.readServersFromFile()
        val oldServers = preferencesProvider.getAllServers()
        val servers = updateOldServers(oldServers, newServers)
        _serverListLiveData.postValue(servers)
        serversProvider.saveServersToPreferences(servers)
        preferencesProvider.saveAppVersion(BuildConfig.VERSION_NAME)
    }

    private fun removeServers(
        serverList: ArrayList<ServerConfig>,
        config: ServerConfig
    ): ArrayList<ServerConfig> {
        serverList.remove(config)
        return serverList
    }

    private fun updateServers(
        serverList: ArrayList<ServerConfig>,
        newServer: ServerConfig
    ): ArrayList<ServerConfig> {
        val serversMap = HashMap<String?, ServerConfig>()
        serverList.forEach {
            serversMap[it.name] = it
        }
        serversMap[newServer.name] = newServer
        return ArrayList(serversMap.values)
    }

    private fun updateOldServers(
        oldList: ArrayList<ServerConfig>,
        newList: ArrayList<ServerConfig>
    ): ArrayList<ServerConfig> {
        val serversMap = HashMap<String?, ServerConfig>()
        oldList.forEach {
            serversMap[it.name] = it
        }
        newList.forEach {
            serversMap[it.name] = it
        }
        return ArrayList(serversMap.values)
    }
}
