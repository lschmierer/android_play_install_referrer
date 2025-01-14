package de.lschmierer.android_play_install_referrer

import android.content.Context
import androidx.annotation.NonNull
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlin.collections.ArrayList


/** AndroidPlayInstallReferrerPlugin */
class AndroidPlayInstallReferrerPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var context: Context
    private lateinit var channel: MethodChannel
    private val pendingResults = ArrayList<Result>(1)
    private var referrerClient: InstallReferrerClient? = null
    private var referrerDetails: ReferrerDetails? = null
    private var referrerError: Pair<String, String>? = null

    private val isInstallReferrerPending: Boolean
        @Synchronized
        get() {
            return referrerClient != null && !isInstallReferrerResolved
        }

    private val isInstallReferrerResolved: Boolean
        @Synchronized
        get() {
            return referrerDetails != null || referrerError != null
        }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.context = flutterPluginBinding.applicationContext
        channel = MethodChannel(
            flutterPluginBinding.binaryMessenger,
            "de.lschmierer.android_play_install_referrer")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getInstallReferrer") {
            getInstallReferrer(result)
        } else {
            result.notImplemented()
        }

    }

    @Synchronized
    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        pendingResults.clear()
        referrerClient?.endConnection()
        channel.setMethodCallHandler(null)
    }

    @Synchronized
    private fun getInstallReferrer(@NonNull result: Result) {
        if (isInstallReferrerResolved) {
            resolveInstallReferrerResult(result)
        } else {
            pendingResults.add(result)

            if (!isInstallReferrerPending) {
                referrerClient = InstallReferrerClient.newBuilder(context).build()
                try {
                    referrerClient?.startConnection(object : InstallReferrerStateListener {
                        override fun onInstallReferrerSetupFinished(responseCode: Int) {
                            handleOnInstallReferrerSetupFinished(responseCode)
                        }

                        override fun onInstallReferrerServiceDisconnected() {
                            Log.w("InstallReferrer", "Service disconnected, retrying...")
                            retryConnection()
                        }
                    })
                } catch (e: DeadObjectException) {
                    Log.e("InstallReferrer", "DeadObjectException caught: ${e.message}", e)
                    referrerError = Pair("DEAD_OBJECT_EXCEPTION", "Service connection lost unexpectedly.")
                    resolvePendingInstallReferrerResults()
                }
            }
        }
    }

    @Synchronized
    private fun handleOnInstallReferrerSetupFinished(responseCode: Int) {
        try {
            when (responseCode) {
                InstallReferrerClient.InstallReferrerResponse.OK -> {
                    referrerClient?.let {
                        try {
                            referrerDetails = it.installReferrer
                        } catch (e: DeadObjectException) {
                            Log.e("InstallReferrer", "DeadObjectException while fetching referrer: ${e.message}", e)
                            referrerError = Pair("DEAD_OBJECT_EXCEPTION", "Service connection lost while retrieving data.")
                        }
                    } ?: run {
                        referrerError = Pair("BAD_STATE", "Referrer client is null.")
                    }
                }
                InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED -> {
                    referrerError = Pair("SERVICE_DISCONNECTED", "Play Store service is not connected now.")
                }
                InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                    referrerError = Pair("SERVICE_UNAVAILABLE", "Connection couldn't be established.")
                }
                InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                    referrerError = Pair("FEATURE_NOT_SUPPORTED", "API not available on the current Play Store app.")
                }
                InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR -> {
                    referrerError = Pair("DEVELOPER_ERROR", "General errors caused by incorrect usage.")
                }
                InstallReferrerClient.InstallReferrerResponse.PERMISSION_ERROR -> {
                    referrerError = Pair("PERMISSION_ERROR", "App is not allowed to bind to the Service.")
                }
                else -> {
                    referrerError = Pair("UNKNOWN_ERROR", "InstallReferrerClient returned unknown response code.")
                }
            }
        } catch (e: Exception) {
            Log.e("InstallReferrer", "Unexpected error: ${e.message}", e)
            referrerError = Pair("UNEXPECTED_ERROR", "An unexpected error occurred.")
        }

        resolvePendingInstallReferrerResults()
        referrerClient?.endConnection()
    }

    @Synchronized
    private fun resolvePendingInstallReferrerResults() {
        pendingResults.forEach {
            resolveInstallReferrerResult(it)
        }
        pendingResults.clear()
    }

    @Synchronized
    private fun resolveInstallReferrerResult(@NonNull result: Result) {
        referrerDetails?.let {
            result.success(
                mapOf(
                    "installReferrer" to it.installReferrer,
                    "referrerClickTimestampSeconds" to it.referrerClickTimestampSeconds,
                    "installBeginTimestampSeconds" to it.installBeginTimestampSeconds,
                    "referrerClickTimestampServerSeconds" to it.referrerClickTimestampServerSeconds,
                    "installBeginTimestampServerSeconds" to it.installBeginTimestampServerSeconds,
                    "installVersion" to it.installVersion,
                    "googlePlayInstantParam" to it.googlePlayInstantParam
                )
            )
            return
        }
        referrerError?.let {
            result.error(it.first, it.second, null)
            return
        }
    }

    private fun retryConnection() {
        try {
            referrerClient = InstallReferrerClient.newBuilder(context).build()
            referrerClient?.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    handleOnInstallReferrerSetupFinished(responseCode)
                }

                override fun onInstallReferrerServiceDisconnected() {
                    Log.w("InstallReferrer", "Retry failed, service disconnected again.")
                }
            })
        } catch (e: Exception) {
            Log.e("InstallReferrer", "Failed to retry connection: ${e.message}", e)
        }
    }
}
