import 'package:flutter/material.dart';
import 'package:qr_code_scanner_app/modules/qrscanner/qrscanner_view.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData.dark(),
      home: const QRScannerView(),
    );
  }
}
