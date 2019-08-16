import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:zk_finger/zk_finger.dart';

void main() {
  const MethodChannel channel = MethodChannel('zk_finger');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await ZkFinger.platformVersion, '42');
  });
}
