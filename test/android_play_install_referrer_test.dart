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
        'referrerClickTimestampServerSeconds': 3,
        'installBeginTimestampServerSeconds': 4,
        'installVersion': null,
        'googlePlayInstantParam': true,
      };
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getInstallReferrer', () async {
    expect((await AndroidPlayInstallReferrer.installReferrer).installReferrer,
        'abc');
    expect(
        (await AndroidPlayInstallReferrer.installReferrer)
            .referrerClickTimestampSeconds,
        1);
    expect(
        (await AndroidPlayInstallReferrer.installReferrer)
            .installBeginTimestampSeconds,
        2);
    expect(
        (await AndroidPlayInstallReferrer.installReferrer)
            .referrerClickTimestampServerSeconds,
        3);
    expect(
        (await AndroidPlayInstallReferrer.installReferrer)
            .installBeginTimestampServerSeconds,
        4);
    expect((await AndroidPlayInstallReferrer.installReferrer).installVersion,
        null);
    expect(
        (await AndroidPlayInstallReferrer.installReferrer)
            .googlePlayInstantParam,
        true);
  });
}
