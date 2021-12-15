package de.lschmierer.android_play_install_referrer

import android.content.Context
import androidx.annotation.NonNull
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar


/** AndroidPlayInstallReferrerPlugin */
class AndroidPlayInstallReferrerPlugin : FlutterPlugin, MethodCallHandler {
    private val CHANNEL_NAME = "de.lschmierer.android_play_install_referrer"

    private lateinit var context: Context
    private var channel: MethodChannel? = null
    private val referrerClients = HashSet<InstallReferrerClient>()

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.context = context
        channel = MethodChannel(messenger, CHANNEL_NAME)
        channel!!.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getInstallReferrer") {
            getInstallReferrer(call, result)
        } else {
            result.notImplemented()
        }

    }

    fun getInstallReferrer(@NonNull call: MethodCall, @NonNull result: Result) {
        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClients.add(referrerClient)
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {

                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        val referrerDetails = referrerClient.installReferrer

                        val details = HashMap<String, Any?>()
                        details["installReferrer"] = referrerDetails.installReferrer
                        details["referrerClickTimestampSeconds"] = referrerDetails.referrerClickTimestampSeconds
                        details["installBeginTimestampSeconds"] = referrerDetails.installBeginTimestampSeconds
                        details["referrerClickTimestampServerSeconds"] = referrerDetails.referrerClickTimestampServerSeconds
                        details["installBeginTimestampServerSeconds"] = referrerDetails.installBeginTimestampServerSeconds
                        details["installVersion"] = referrerDetails.installVersion
                        details["googlePlayInstantParam"] = referrerDetails.googlePlayInstantParam

                        result.success(details)
                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        result.error("FEATURE_NOT_SUPPORTED", "API not available on the current Play Store app.", null)
                        return
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        result.error("SERVICE_UNAVAILABLE", "Connection couldn't be established.", null)
                        return
                    }
                    InstallReferrerClient.InstallReferrerResponse.PERMISSION_ERROR -> {
                        result.error("PERMISSION_ERROR", "App is not allowed to bind to the Service.", null)
                        return
                    }
                }
                referrerClients.remove(referrerClient)
                referrerClient.endConnection()
            }

            override fun onInstallReferrerServiceDisconnected() {}
        })
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        referrerClients.forEach { it.endConnection() }
        referrerClients.clear()
        channel?.setMethodCallHandler(null)
        channel = null
    }
}
