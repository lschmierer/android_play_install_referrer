package de.lschmierer.android_play_install_referrer

import android.content.Context
import androidx.annotation.NonNull
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** AndroidPlayInstallReferrerPlugin */
class AndroidPlayInstallReferrerPlugin : FlutterPlugin, MethodCallHandler, InstallReferrerStateListener {
    private val CHANNEL_NAME = "de.lschmierer.android_play_install_referrer"

    private lateinit var context: Context
    private var channel: MethodChannel? = null
    private var referrerClient: InstallReferrerClient? = null;
    private var currentResult: Result? = null;

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        setupChannel(flutterPluginBinding.binaryMessenger, flutterPluginBinding.applicationContext)
    }

    private fun setupChannel(messenger: BinaryMessenger, context: Context) {
        this.context = context
        channel = MethodChannel(messenger, CHANNEL_NAME)
        channel!!.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getInstallReferrer") {
            getInstallReferrer(result)
        } else {
            result.notImplemented()
        }
    }

    private fun getInstallReferrer(@NonNull result: Result) {
        currentResult = result;
        referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient!!.startConnection(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        channel = null
        referrerClient = null;
        currentResult = null;
    }

    override fun onInstallReferrerSetupFinished(responseCode: Int) {
        when (responseCode) {
            InstallReferrerClient.InstallReferrerResponse.OK -> {
                val referrerDetails = referrerClient?.installReferrer

                if (referrerDetails != null) {
                    val details = HashMap<String, Any?>()
                    details["installReferrer"] = referrerDetails.installReferrer
                    details["referrerClickTimestampSeconds"] = referrerDetails.referrerClickTimestampSeconds
                    details["installBeginTimestampSeconds"] = referrerDetails.installBeginTimestampSeconds
                    details["referrerClickTimestampServerSeconds"] = referrerDetails.referrerClickTimestampServerSeconds
                    details["installBeginTimestampServerSeconds"] = referrerDetails.installBeginTimestampServerSeconds
                    details["installVersion"] = referrerDetails.installVersion
                    details["googlePlayInstantParam"] = referrerDetails.googlePlayInstantParam

                    currentResult?.success(details)
                } else {
                    currentResult?.error("BAD_STATE", "Result is null.", null);
                }
            }
            InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                currentResult?.error("FEATURE_NOT_SUPPORTED", "API not available on the current Play Store app.", null)
                return
            }
            InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                currentResult?.error("SERVICE_UNAVAILABLE", "Connection couldn't be established.", null)
                return
            }
            InstallReferrerClient.InstallReferrerResponse.PERMISSION_ERROR -> {
                currentResult?.error("PERMISSION_ERROR", "App is not allowed to bind to the Service.", null)
                return
            }
        }
        referrerClient?.endConnection()
        referrerClient = null;
    }

    override fun onInstallReferrerServiceDisconnected() {
        referrerClient = null;
        currentResult = null;
    }
}
