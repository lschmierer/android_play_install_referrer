import Flutter
import UIKit

public class SwiftAndroidPlayInstallReferrerPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "android_play_install_referrer", binaryMessenger: registrar.messenger())
    let instance = SwiftAndroidPlayInstallReferrerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
