import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:qr_code_scanner_app/channels/method_channel_handler.dart';

class QRScannerController extends GetxController {
  final cameraChannel = MethodChannelHandler().cameraChannel;

  var qrResult = 'No QR code detected'.obs;

  var isCameraVisible = false.obs;

  var qrData = Rx<Map<String, dynamic>?>(null);
  
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

void onPlatformViewCreated(int id) async {
    final channel = MethodChannel('dvnthn.qrscanner/camera_view_$id');
    
    // Đăng ký handler để nhận dữ liệu từ native khi mã QR được quét
    channel.setMethodCallHandler((call) async {
      if (call.method == "onQRCodeDetected") {
        Map<String, dynamic> data = Map<String, dynamic>.from(call.arguments);
        qrResult.value = data["qrValue"];
        qrData.value = data;
        print("QR Data: $data");
      }
    });
    
    try {
      final result = await channel.invokeMethod('initialize');
      print("Native result: $result");
    } on PlatformException catch (e) {
      if (e.code == "PERMISSION_NOT_GRANTED") {
        _hideCamera();
        print("Camera permission not granted");
      } else {
        print("Error initializing camera: ${e.message}");
      }
    }
  }

  void showCamera() {
    isCameraVisible.value = true;
  }

  _hideCamera() {
    isCameraVisible.value = false;
  }
}
