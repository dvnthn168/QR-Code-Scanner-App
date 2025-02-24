package dvnthn.qrscanner

import com.dvnthn.qrscanner.channel.CameraPreviewFactory
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity(){
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // init channel
//        MethodChannelHandler(this, flutterEngine.dartExecutor.binaryMessenger)

        flutterEngine.platformViewsController.registry.registerViewFactory(
            "dvnthn.qrscanner/camera_view",
            CameraPreviewFactory(flutterEngine.dartExecutor.binaryMessenger, this)
        )
    }
}
