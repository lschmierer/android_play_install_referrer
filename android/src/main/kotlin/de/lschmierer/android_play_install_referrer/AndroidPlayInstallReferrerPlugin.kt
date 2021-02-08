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

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        setupChannel(flutterPluginBinding.binaryMessenger, flutterPluginBinding.applicationContext)
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            AndroidPlayInstallReferrerPlugin().setupChannel(registrar.messenger(), registrar.context())
        }
    }

    private fun setupChannel(messenger: BinaryMessenger, context: Context) {
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
                referrerClient.endConnection()
            }

            override fun onInstallReferrerServiceDisconnected() {}
        })
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        channel = null
    }
}
