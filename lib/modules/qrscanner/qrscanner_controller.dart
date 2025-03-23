import 'dart:isolate';

import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:qr_code_scanner_app/channels/method_channel_handler.dart';
import 'package:qr_code_scanner_app/db/database_helper.dart';

class QRScannerController extends GetxController {
  final cameraChannel = MethodChannelHandler().cameraChannel;

  var qrResult = 'No QR code detected'.obs;

  var isCameraVisible = false.obs;

  var qrData = Rx<Map<String, dynamic>?>(null);

  var qrDataHistory = <Map<String, dynamic>>[].obs;

  final DatabaseHelper dbHelper = DatabaseHelper();

  Set qrContentHistory = {};

  @override
  void onInit() {
    super.onInit();
    loadQRCodeHistory();
  }

  Future<void> loadQRCodeHistory() async {
    final data = await dbHelper.getScanHistory();
    qrDataHistory.assignAll(data);
    qrDataHistory.forEach((item) => qrContentHistory.add(item["content"]));
  }

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

  void onPlatformViewCreated(int id) async {
    final channel = MethodChannel('dvnthn.qrscanner/camera_view_$id');

    channel.setMethodCallHandler((call) async {
      if (call.method == "onQRCodeDetected") {
        Map<String, dynamic> data = Map<String, dynamic>.from(call.arguments);
        qrResult.value = data["qrValue"];
        qrData.value = data;

        handleQRCodeInBackground(data, dbHelper);
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

  Future<void> handleQRCodeInBackground(
    Map<String, dynamic> data,
    DatabaseHelper dbHelper,
  ) async {
    if (!qrContentHistory.contains(data["qrValue"])) {
      await dbHelper.insertScan(data["qrValue"]);
      await loadQRCodeHistory();
    }
  }

  void showCamera() {
    isCameraVisible.value = true;
  }

  _hideCamera() {
    isCameraVisible.value = false;
  }
}
