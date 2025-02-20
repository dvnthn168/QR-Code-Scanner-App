import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:qr_code_scanner_app/channels/method_channel_handler.dart';

class QRScannerController extends GetxController {
  final cameraChannel = MethodChannelHandler().cameraChannel;

  var qrResult = 'No QR code detected'.obs;

  var isCameraVisible = false.obs;

  Future<void> startCamera() async {
    try {
      await cameraChannel.startCamera();
      String? result = await cameraChannel.getResult();
      if (result != null) {
        qrResult.value = result;
      }
    } catch (e) {
      qrResult.value = 'Error: $e';
    }
  }

  Future<void> stopCamera() async {
    try {
      await cameraChannel.stopCamera();
    } catch (e) {}
  }

  void onPlatformViewCreated(int id) {
    final channel = MethodChannel('dvnthn.qrscanner/camera_view_$id');
    channel.invokeMethod('initialize');
  }

  void showCamera() {
    isCameraVisible.value = true;
  }

  void hideCamera() {
    isCameraVisible.value = false;
  }
}
