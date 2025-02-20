package dvnthn.qrscanner.channel

import android.app.Activity
import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

/**
 * Lớp này quản lý các MethodChannel và định tuyến các lời gọi từ Flutter tới các hàm native.
 *
 * @param context Context của Activity, cần dùng để khởi chạy CameraX.
 * @param messenger BinaryMessenger được sử dụng để giao tiếp với Flutter.
 */
class MethodChannelHandler(private val context: Context, messenger: BinaryMessenger) {
    private val CAMERA_CHANNEL = "dvnthn.qrscanner/camera"

    init {
        MethodChannel(messenger, CAMERA_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startCamera" -> {
                    CameraMethodChannel.startCamera(context) { qrResult ->
                        (context as Activity).runOnUiThread {
                            result.success(qrResult)
                        }
                    }
                }

                "stopCamera" -> {
                    CameraMethodChannel.stopCamera()
                    result.success("Camera stopped")
                }

//                "getResult" -> {
//                    result.success(CameraMethodChannel.getQrResult())
//                }

                else -> result.notImplemented()
            }
        }
    }
}
