#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint android_play_install_referrer.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'android_play_install_referrer'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter plugin for the Android Play Install Referrer API'
  s.description      = <<-DESC
A new Flutter plugin for the Android Play Install Referrer API
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '8.0'

  # Flutter.framework does not contain a i386 slice. Only x86_64 simulators are supported.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64' }
  s.swift_version = '5.0'
end
