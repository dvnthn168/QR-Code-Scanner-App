package dvnthn.qrscanner

import android.content.pm.PackageManager
import com.dvnthn.qrscanner.channel.CameraPreviewFactory
import dvnthn.qrscanner.channel.CameraPreviewView
import io.flutter.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity(){
    private var flutterEngineRef: FlutterEngine? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        flutterEngineRef = flutterEngine

        flutterEngine.platformViewsController.registry.registerViewFactory(
            "dvnthn.qrscanner/camera_view",
            CameraPreviewFactory(flutterEngine.dartExecutor.binaryMessenger, this),
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CameraPreviewView.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("CameraPreview", "Camera permission not granted. Requesting permission...")

            } else {

            }
        }
    }
}
