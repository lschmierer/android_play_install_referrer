import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:android_play_install_referrer/android_play_install_referrer.dart';

void main() {
  const MethodChannel channel =
      MethodChannel('de.lschmierer.android_play_install_referrer');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return {
        'installReferrer': 'abc',
        'referrerClickTimestampSeconds': 1,
        'installBeginTimestampSeconds': 2,
        'googlePlayInstantParam': true,
      };
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getInstallReferrer', () async {
    expect(await AndroidPlayInstallReferrer.installReferrer,
        ReferrerDetails('abc', 1, 2, true));
  });
}
