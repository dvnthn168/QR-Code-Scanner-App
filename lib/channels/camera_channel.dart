import 'package:flutter/services.dart';

class CameraChannel {
  static const MethodChannel _channel = MethodChannel(
    'dvnthn.qrscanner/camera',
  );

  Future<void> startCamera() async {
    try {
      await _channel.invokeMethod('initialize');
    } on PlatformException catch (e) {
      throw 'Failed to start camera: ${e.message}';
    }
  }

  Future<void> stopCamera() async {
    try {
      await _channel.invokeMethod('stopCamera');
    } on PlatformException catch (e) {
      throw 'Failed to stop camera: ${e.message}';
    }
  }

  Future<String?> getResult() async {
    try {
      return await _channel.invokeMethod<String>('getResult');
    } on PlatformException catch (e) {
      throw 'Failed to get result: ${e.message}';
    }
  }
}
