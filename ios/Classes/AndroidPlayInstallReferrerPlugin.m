#import "AndroidPlayInstallReferrerPlugin.h"
#if __has_include(<android_play_install_referrer/android_play_install_referrer-Swift.h>)
#import <android_play_install_referrer/android_play_install_referrer-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "android_play_install_referrer-Swift.h"
#endif

@implementation AndroidPlayInstallReferrerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftAndroidPlayInstallReferrerPlugin registerWithRegistrar:registrar];
}
@end
