package im.threads.android.use_cases.developer_options

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.jakewharton.processphoenix.ProcessPhoenix
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Appearance
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.common.configuration.Placement
import com.pandulapeter.beagle.common.configuration.toText
import com.pandulapeter.beagle.common.contracts.BeagleListItemContract
import com.pandulapeter.beagle.logCrash.BeagleCrashLogger
import com.pandulapeter.beagle.logOkHttp.BeagleOkHttpLogger
import com.pandulapeter.beagle.modules.AnimationDurationSwitchModule
import com.pandulapeter.beagle.modules.AppInfoButtonModule
import com.pandulapeter.beagle.modules.BugReportButtonModule
import com.pandulapeter.beagle.modules.DeveloperOptionsButtonModule
import com.pandulapeter.beagle.modules.DeviceInfoModule
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.ForceCrashButtonModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.KeylineOverlaySwitchModule
import com.pandulapeter.beagle.modules.LifecycleLogListModule
import com.pandulapeter.beagle.modules.LogListModule
import com.pandulapeter.beagle.modules.NetworkLogListModule
import com.pandulapeter.beagle.modules.PaddingModule
import com.pandulapeter.beagle.modules.ScreenCaptureToolboxModule
import com.pandulapeter.beagle.modules.SingleSelectionListModule
import com.pandulapeter.beagle.modules.TextModule
import im.threads.android.BuildConfig
import im.threads.android.R
import im.threads.android.core.ThreadsDemoApplication
import im.threads.android.data.ServerConfig
import im.threads.android.data.TransportConfig
import im.threads.android.ui.MainActivity
import im.threads.android.ui.add_server_dialog.AddServerDialog
import im.threads.android.ui.add_server_dialog.AddServerDialogActions
import im.threads.android.utils.PrefUtilsApp
import im.threads.android.utils.fromJson
import im.threads.android.utils.toJson
import im.threads.internal.domain.logger.LoggerEdna
import im.threads.internal.secureDatabase.DatabaseHolder
import java.io.FileOutputStream
import java.io.InputStream

class DebugMenuInteractor(private val context: Context) : DebugMenuUseCase {
    private var isServersListInitialized = false
    private var currentServerName = ""
    private var servers = listOf<ServerMenuItem>()
    private var isAddServerModuleAdded = false

    override fun configureDebugMenu() {
        fetchServerNames()
        Beagle.initialize(
            context as ThreadsDemoApplication,
            appearance = Appearance(
                themeResourceId = R.style.DebugMenuTheme
            ),
            behavior = Behavior(
                bugReportingBehavior = Behavior.BugReportingBehavior(
                    crashLoggers = listOf(BeagleCrashLogger),
                    buildInformation = {
                        listOf(
                            "Version name".toText() to BuildConfig.VERSION_NAME,
                            "Version code".toText() to BuildConfig.VERSION_CODE.toString(),
                            "Application ID".toText() to BuildConfig.APPLICATION_ID
                        )
                    }
                ),
                networkLogBehavior = Behavior.NetworkLogBehavior(
                    networkLoggers = listOf(BeagleOkHttpLogger)
                )
            )
        )
        setModulesToBeagle()
    }

    override fun isServerNotSet() = getLatestServer() == null

    override fun initServer() {
        copyServersFromFile()
        fetchServerNames()
        if (currentServerName.isBlank()) {
            currentServerName = getServers().first().name
        }
        setCurrentServer(currentServerName)
        val currentServerConfig = getCurrentServer()
        PrefUtilsApp.saveTransportConfig(
            context,
            TransportConfig(
                currentServerConfig.serverBaseUrl,
                currentServerConfig.datastoreUrl,
                threadsGateUrl = currentServerConfig.threadsGateUrl,
                threadsGateProviderUid = currentServerConfig.threadsGateProviderUid,
                isNewChatCenterApi = currentServerConfig.newChatCenterApi
            )
        )
    }

    override fun setServerAsChanged() {
        PrefUtilsApp.setIsServerChanged(context, true)
    }

    override fun getCurrentServer(): ServerConfig {
        return getLatestServer()?.let { server ->
            val serverExistInList = getServers().firstOrNull { it.name == server.name } != null
            return if (serverExistInList) {
                server
            } else {
                getDefaultServer()
            }
        } ?: getDefaultServer()
    }

    override fun setCurrentServer(serverName: String) {
        getServers().firstOrNull { it.name == serverName }?.let { serverConfig ->
            PrefUtilsApp.saveTransportConfig(
                context,
                TransportConfig(
                    serverConfig.serverBaseUrl,
                    serverConfig.datastoreUrl,
                    threadsGateUrl = serverConfig.threadsGateUrl,
                    threadsGateProviderUid = serverConfig.threadsGateProviderUid,
                    isNewChatCenterApi = serverConfig.newChatCenterApi
                )
            )
            PrefUtilsApp.setCurrentServer(context, serverName)
        } ?: LoggerEdna.error("Cannot set server!")
    }

    override fun getServers(): List<ServerConfig> {
        return PrefUtilsApp
            .getAllServers(context)
            .map { Gson().fromJson<ServerConfig>(it.value) }
    }

    override fun addServer(serverConfig: ServerConfig) {
        val map = hashMapOf(Pair(serverConfig.name, serverConfig.toJson()))
        PrefUtilsApp.addServers(context, map)
    }

    override fun addUiDependedModulesToDebugMenu(activity: AppCompatActivity) {
        if (!isAddServerModuleAdded) {
            val paddingModule = PaddingModule(PaddingModule.Size.MEDIUM)
            val addServerModule = TextModule(
                getString(R.string.demo_add_server),
                TextModule.Type.BUTTON,
                onItemSelected = {
                    val onServerAddedAction = object : AddServerDialogActions {
                        override fun onServerAdded() {
                            fetchServerNames()
                            setModulesToBeagle()
                            addUiDependedModulesToDebugMenu(activity)
                        }
                    }
                    AddServerDialog.open(activity, onServerAddedAction)
                }
            )
            Beagle.add(
                paddingModule,
                addServerModule,
                placement = Placement.Below(SINGLE_SELECTION_MODULE_ID)
            )
            isAddServerModuleAdded = true
        }
    }

    private fun copyServersFromFile() {
        val `in`: InputStream = context.resources.openRawResource(R.raw.servers_config)
        val out = FileOutputStream(context.filesDir.parent + "/shared_prefs/servers_config.xml")
        val buff = ByteArray(1024)
        var read = 0

        try {
            while (`in`.read(buff).also { read = it } > 0) {
                out.write(buff, 0, read)
            }
        } finally {
            `in`.close()
            out.close()
        }
        PrefUtilsApp.applyServersFromFile(context)
    }

    private fun setModulesToBeagle() {
        Beagle.set(
            HeaderModule(
                title = getCurrentServerTitle(),
                subtitle = BuildConfig.APPLICATION_ID,
                text = "${BuildConfig.BUILD_TYPE} v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            ),
            PaddingModule(size = PaddingModule.Size.LARGE),
            BugReportButtonModule(),
            ScreenCaptureToolboxModule(),
            DividerModule(),
            TextModule(getString(R.string.demo_server_selection), TextModule.Type.SECTION_HEADER),
            SingleSelectionListModule(
                id = SINGLE_SELECTION_MODULE_ID,
                title = currentServerName,
                items = servers,
                isExpandedInitially = false,
                isValuePersisted = true,
                initiallySelectedItemId = currentServerName,
                onSelectionChanged = { onServerChanged(it) }
            ),
            DividerModule(),
            TextModule("Logs", TextModule.Type.SECTION_HEADER),
            NetworkLogListModule(),
            LogListModule(maxItemCount = 100),
            LifecycleLogListModule(),
            DividerModule(),
            TextModule("Debug", TextModule.Type.SECTION_HEADER),
            AnimationDurationSwitchModule(),
            KeylineOverlaySwitchModule(),
            DeviceInfoModule(),
            DeveloperOptionsButtonModule(),
            PaddingModule(size = PaddingModule.Size.LARGE),
            AppInfoButtonModule(getString(R.string.about_app).toText()),
            ForceCrashButtonModule(),
        )
    }

    private fun onServerChanged(serverMenuItem: ServerMenuItem?) {
        if (isServersListInitialized) {
            serverMenuItem?.let {
                currentServerName = it.name.toString()
                setCurrentServer(it.name.toString())
                cleanHistory()
                Toast.makeText(
                    context,
                    getString(R.string.demo_restart_app_for_server_apply),
                    Toast.LENGTH_SHORT
                ).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    ProcessPhoenix.triggerRebirth(context, Intent(context, MainActivity::class.java))
                }, 2000)
            }
        }
        isServersListInitialized = true
    }

    private fun getLatestServer(): ServerConfig? {
        val preferencesMap = PrefUtilsApp.getAllServers(context)
        return preferencesMap[PrefUtilsApp.getCurrentServer(context)]?.let {
            Gson().fromJson<ServerConfig>(it)
        }
    }

    private fun fetchServerNames() {
        currentServerName = getCurrentServer().name
        servers = getServers()
            .map { ServerMenuItem(it.name) }
            .sortedBy { it.name.toString() }
    }

    private fun getCurrentServerTitle() = PrefUtilsApp.getCurrentServer(context)

    private fun getDefaultServer(): ServerConfig {
        return PrefUtilsApp
            .getAllServers(context)
            .map { Gson().fromJson<ServerConfig>(it.value) }
            .first()
    }

    private fun cleanHistory() {
        DatabaseHolder.getInstance().cleanDatabase()
    }

    private fun getString(resId: Int) = context.getString(resId)

    data class ServerMenuItem(val name: CharSequence) : BeagleListItemContract {
        override val title = name.toText()
    }

    companion object {
        private const val SINGLE_SELECTION_MODULE_ID = "singleSelectionModuleId"
    }
}
