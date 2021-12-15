package de.lschmierer.android_play_install_referrer

import android.content.Context
import androidx.annotation.NonNull
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.*


@ExperimentalCoroutinesApi
/** AndroidPlayInstallReferrerPlugin */
class AndroidPlayInstallReferrerPlugin : FlutterPlugin, MethodCallHandler {

    companion object {
        const val CHANNEL_NAME = "de.lschmierer.android_play_install_referrer"
    }

    private lateinit var context: Context
    private var channel: MethodChannel? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL_NAME)
        channel!!.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        channel = null
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getInstallReferrer") {
            val client = InstallReferrerClient.newBuilder(context).build()
            runBlocking {
                val res = withContext(Dispatchers.Default) { awaitCallback(client) }
                when (res.first) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        if (res.second.isNotEmpty()) {
                            result.success(res.second)
                        } else {
                            result.error("BAD_STATE", "Result is null.", null)
                        }
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        result.error("FEATURE_NOT_SUPPORTED", "API not available on the current Play Store app.", null)
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        result.error("SERVICE_UNAVAILABLE", "Connection couldn't be established.", null)
                    }
                    InstallReferrerClient.InstallReferrerResponse.PERMISSION_ERROR -> {
                        result.error("PERMISSION_ERROR", "App is not allowed to bind to the Service.", null)
                    }
                }
            }
        } else {
            result.notImplemented()
        }
    }


    private suspend fun awaitCallback(client: InstallReferrerClient): Pair<Int, Map<String, Any?>> = suspendCancellableCoroutine { continuation ->
        val callback = object : InstallReferrerStateListener { // Implementation of some callback interface
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                val details = HashMap<String, Any?>()

                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    val referrerDetails = client.installReferrer

                    if (referrerDetails != null) {
                        details["installReferrer"] = referrerDetails.installReferrer
                        details["referrerClickTimestampSeconds"] = referrerDetails.referrerClickTimestampSeconds
                        details["installBeginTimestampSeconds"] = referrerDetails.installBeginTimestampSeconds
                        details["referrerClickTimestampServerSeconds"] = referrerDetails.referrerClickTimestampServerSeconds
                        details["installBeginTimestampServerSeconds"] = referrerDetails.installBeginTimestampServerSeconds
                        details["installVersion"] = referrerDetails.installVersion
                        details["googlePlayInstantParam"] = referrerDetails.googlePlayInstantParam
                    }
                }
                client.endConnection()
                // Resume coroutine with a value provided by the callback
                continuation.resume(Pair(responseCode, details)) { client.endConnection() }
            }

            override fun onInstallReferrerServiceDisconnected() {
            }
        }
        // Register callback with an API
        client.startConnection(callback)
        // Remove callback on cancellation
    }

}
