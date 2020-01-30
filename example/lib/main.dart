import 'dart:async';

import 'package:flutter/material.dart';

import 'package:android_play_install_referrer/android_play_install_referrer.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _referrerDetails = '';

  @override
  void initState() {
    super.initState();
    initReferrerDetails();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initReferrerDetails() async {
    String referrerDetailsString;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      ReferrerDetails referrerDetails = await AndroidPlayInstallReferrer.installReferrer;

      referrerDetailsString = referrerDetails.toString();
    } catch (e) {
      referrerDetailsString = 'Failed to get referrer details: $e';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _referrerDetails = referrerDetailsString;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Referrer Details: $_referrerDetails'),
        ),
      ),
    );
  }
}
