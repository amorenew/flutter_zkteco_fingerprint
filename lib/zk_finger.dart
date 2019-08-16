import 'dart:async';

import 'package:flutter/services.dart';

class ZkFinger {
  static const MethodChannel _channel = const MethodChannel('zkfinger');

  static const EventChannel statusChangeStream =
      EventChannel('com.zkteco.zkfinger/status_change');
  static const EventChannel imageStream =
      EventChannel('com.zkteco.zkfinger/finger_image');

  static Future<String> get platformVersion async {
    return _channel.invokeMethod('getPlatformVersion');
  }

  static Future<bool> openConnection() async {
    return _channel.invokeMethod('openConnection');
  }

  static Future<bool> closeConnection() async {
    return _channel.invokeMethod('closeConnection');
  }

  static Future<bool> startListen({String userId}) async {
    return _channel.invokeMethod('startListen', <String, String>{'id': userId});
  }

  static Future<bool> stopListen() async {
    return _channel.invokeMethod('stopListen');
  }

  static Future<bool> enroll({String userId}) async {
    return _channel.invokeMethod('enroll', <String, String>{'id': userId});
  }

  static Future<bool> verify({String userId}) async {
    return _channel.invokeMethod('verify', <String, String>{'id': userId});
  }

  static Future<bool> registerFinger({String userId, String dataBase64}) async {
    final bool success = await _channel.invokeMethod(
        'register', <String, String>{'id': userId, 'data': dataBase64});
    return success;
  }

  static Future<bool> clearFingerDatabase() async {
    return await _channel.invokeMethod('clear');
  }
}
