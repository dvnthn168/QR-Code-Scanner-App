import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:get/get_state_manager/get_state_manager.dart';
import 'package:qr_code_scanner_app/modules/qrscanner/qrscanner_controller.dart';

class QRScannerView extends StatelessWidget {
  const QRScannerView({super.key});

  @override
  Widget build(BuildContext context) {
    return GetBuilder(
      init: QRScannerController(),
      builder: (controller) {
        return Scaffold(
          appBar: AppBar(
            title: const Text(
              'QR Code Scanner',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            centerTitle: true,
            elevation: 0,
          ),
          body: Column(
            children: [
              _cameraPreview(controller),

              Obx(() => qrCodeResult(controller.qrResult.value)),

              _scanButton(controller),

              const SizedBox(height: 16),

              _scannerHistory(),
            ],
          ),
        );
      },
    );
  }

  Widget _cameraPreview(QRScannerController controller) {
    return Obx(
      () =>
          controller.isCameraVisible.value
              ? _qrCodePreview(controller)
              : Expanded(
                flex: 3,
                child: Container(
                  decoration: BoxDecoration(
                    color: Colors.black,
                    borderRadius: const BorderRadius.only(
                      bottomLeft: Radius.circular(24),
                      bottomRight: Radius.circular(24),
                    ),
                  ),
                  alignment: Alignment.center,
                  child: const Icon(
                    Icons.qr_code_scanner,
                    color: Colors.white,
                    size: 100,
                  ),
                ),
              ),
    );
  }

  Widget _qrCodePreview(QRScannerController controller) {
    return Expanded(
      flex: 3,
      child: LayoutBuilder(
        builder: (context, constraints) {
          final cameraWidth =
              (controller.qrData.value?["cameraWidth"] ?? 1 as num).toDouble();
          final cameraHeight =
              (controller.qrData.value?["cameraHeight"] ?? 1 as num).toDouble();
          double scaleX = constraints.maxWidth / cameraWidth;
          double scaleY = constraints.maxHeight / cameraHeight;
          print("scaleX: $scaleX, scaleY: $scaleY");

          return Stack(
            children: [
              AndroidView(
                viewType: 'dvnthn.qrscanner/camera_view',
                onPlatformViewCreated: controller.onPlatformViewCreated,
                layoutDirection: TextDirection.ltr,
                hitTestBehavior: PlatformViewHitTestBehavior.transparent,
              ),

              if (controller.qrData.value != null)
                Positioned(
                  left:
                      (controller.qrData.value!["boundingBox"]["left"] as num)
                          .toDouble() *
                      scaleX,
                  top:
                      (controller.qrData.value!["boundingBox"]["top"] as num)
                          .toDouble() *
                      scaleY,

                  child: Container(
                    width:
                        (controller.qrData.value!["boundingBox"]["width"]
                                as num)
                            .toDouble() *
                        scaleX,
                    height:
                        (controller.qrData.value!["boundingBox"]["height"]
                                as num)
                            .toDouble() *
                        scaleY,
                    decoration: BoxDecoration(
                      border: Border.all(color: Colors.green, width: 2),
                    ),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }

  Widget qrCodeResult(String result) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(borderRadius: BorderRadius.circular(16)),
      margin: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'QR Code Result',
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          ),
          SizedBox(height: 8),
          Text(result, style: TextStyle(color: Colors.grey, fontSize: 16)),
        ],
      ),
    );
  }

  Widget _scanButton(QRScannerController controller) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: ElevatedButton.icon(
        onPressed: () {
          controller.showCamera();
        },
        style: ElevatedButton.styleFrom(
          minimumSize: const Size(double.infinity, 50),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
        ),
        icon: const Icon(Icons.camera_alt),
        label: const Text('Scan QR Code', style: TextStyle(fontSize: 18)),
      ),
    );
  }

  Widget _scannerHistory() {
    return Expanded(
      flex: 2,
      child: Container(
        padding: const EdgeInsets.all(16),

        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Scan History',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Expanded(
              child: ListView(
                children: const [
                  ListTile(
                    leading: Icon(Icons.qr_code, color: Colors.blueAccent),
                    title: Text('QR Code 1: Example Data'),
                    trailing: Icon(Icons.arrow_forward_ios, size: 16),
                  ),
                  ListTile(
                    leading: Icon(Icons.qr_code, color: Colors.blueAccent),
                    title: Text('QR Code 2: Example Data'),
                    trailing: Icon(Icons.arrow_forward_ios, size: 16),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
