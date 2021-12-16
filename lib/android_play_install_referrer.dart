import 'dart:async';

import 'package:flutter/services.dart';

/// Google Play Install Referrer Details.
class ReferrerDetails {
  ReferrerDetails(
    this._installReferrer,
    this._referrerClickTimestampSeconds,
    this._installBeginTimestampSeconds,
    this._referrerClickTimestampServerSeconds,
    this._installBeginTimestampServerSeconds,
    this._installVersion,
    this._googlePlayInstantParam,
  );

  final String? _installReferrer;
  final int _referrerClickTimestampSeconds;
  final int _installBeginTimestampSeconds;
  final int _referrerClickTimestampServerSeconds;
  final int _installBeginTimestampServerSeconds;
  final String? _installVersion;
  final bool _googlePlayInstantParam;

  /// The referrer URL of the installed package.
  String? get installReferrer {
    return _installReferrer;
  }

  /// The timestamp in seconds when referrer click happens.
  int get referrerClickTimestampSeconds {
    return _referrerClickTimestampSeconds;
  }

  /// The timestamp in seconds when installation begins.
  int get installBeginTimestampSeconds {
    return _installBeginTimestampSeconds;
  }

  /// The server-side timestamp, in seconds, when the referrer click happened.
  int get referrerClickTimestampServerSeconds {
    return _referrerClickTimestampServerSeconds;
  }

  /// The server-side timestamp, in seconds, when app installation began.
  int get installBeginTimestampServerSeconds {
    return _installBeginTimestampServerSeconds;
  }

  /// The app's version at the time when the app was first installed.
  String? get installVersion {
    return _installVersion;
  }

  /// Indicates whether your app's instant experience was launched within the past 7 days.
  bool get googlePlayInstantParam {
    return _googlePlayInstantParam;
  }

  @override
  bool operator ==(o) {
    return o is ReferrerDetails &&
        o.installReferrer == installReferrer &&
        o.referrerClickTimestampSeconds == referrerClickTimestampSeconds &&
        o.installBeginTimestampSeconds == installBeginTimestampSeconds &&
        o.googlePlayInstantParam == googlePlayInstantParam;
  }

  @override
  int get hashCode {
    return installReferrer.hashCode ^
        referrerClickTimestampSeconds.hashCode ^
        installBeginTimestampSeconds.hashCode ^
        googlePlayInstantParam.hashCode;
  }

  @override
  String toString() {
    return 'ReferrerDetails { '
        'installReferrer: $installReferrer, '
        'referrerClickTimestampSeconds: $referrerClickTimestampSeconds, '
        'installBeginTimestampSeconds: $installBeginTimestampSeconds, '
        'googlePlayInstantParam: $googlePlayInstantParam }';
  }
}

class AndroidPlayInstallReferrer {
  static const MethodChannel _channel =
      const MethodChannel('de.lschmierer.android_play_install_referrer');

  /// Get installation referrer details.
  ///
  /// Throws an exception on iOS and if Google Play Services are not available on Android.
  static Future<ReferrerDetails> get installReferrer async {
    final Map details = await _channel.invokeMethod('getInstallReferrer');

    return ReferrerDetails(
      details['installReferrer'],
      details['referrerClickTimestampSeconds'],
      details['installBeginTimestampSeconds'],
      details['referrerClickTimestampServerSeconds'],
      details['installBeginTimestampServerSeconds'],
      details['installVersion'],
      details['googlePlayInstantParam'],
    );
  }
}
