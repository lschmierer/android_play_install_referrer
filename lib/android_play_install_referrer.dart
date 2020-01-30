import 'dart:async';

import 'package:flutter/services.dart';

/// Google Play Install Referrer Details.
class ReferrerDetails {
  ReferrerDetails(this._installReferrer, this._referrerClickTimestampSeconds,
      this._installBeginTimestampSeconds, this._googlePlayInstantParam);

  final String _installReferrer;
  final int _referrerClickTimestampSeconds;
  final int _installBeginTimestampSeconds;
  final bool _googlePlayInstantParam;

  /// The referrer URL of the installed package.
  String get installReferrer {
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

  /// Boolean indicating if the user has interacted with the app's instant experience in the past 7 days.
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

    if (details == null) {
      return null;
    }

    return ReferrerDetails(
        details['installReferrer'],
        details['referrerClickTimestampSeconds'],
        details['installBeginTimestampSeconds'],
        details['googlePlayInstantParam']);
  }
}
