import 'camera_channel.dart';

class MethodChannelHandler {
  static final MethodChannelHandler _instance =
      MethodChannelHandler._internal();

  factory MethodChannelHandler() {
    return _instance;
  }

  MethodChannelHandler._internal();

  final cameraChannel = CameraChannel();

  Future<void> initialize() async {}
}
