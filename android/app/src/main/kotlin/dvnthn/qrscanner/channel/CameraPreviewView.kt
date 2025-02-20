// CameraPreviewView.kt
package dvnthn.qrscanner.channel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import io.flutter.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class CameraPreviewView(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    id: Int,
    messenger: BinaryMessenger
) : PlatformView {


    private val previewView: PreviewView = PreviewView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    // Biến để kiểm tra xem camera đã được khởi tạo chưa
    private var initialized = false

    // Tạo MethodChannel riêng cho view này với tên chứa id
    private val channel: MethodChannel =
        MethodChannel(messenger, "dvnthn.qrscanner/camera_view_$id")

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
    }


    init {
        // Lắng nghe yêu cầu initialize từ Flutter
        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "initialize" -> {
                    initializeCamera()
                    result.success("Initialized")
                }

                else -> result.notImplemented()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            }
            Log.e("CameraPreview", "Camera permission not granted, requesting permission.")
            false
        } else {
            true
        }
    }

    private fun initializeCamera() {
        if (!initialized) {

            if (!checkCameraPermission()) {
                return
            }


            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    // Lấy ProcessCameraProvider một cách an toàn
                    val cameraProvider = cameraProviderFuture.get()

                    // Tạo preview và gán surfaceProvider cho PreviewView
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // Chọn camera sau (BACK)
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    // Bind preview vào lifecycle của Activity
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                    initialized = true
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Error initializing camera: ${e.message}", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }


    override fun getView(): View = previewView

    override fun dispose() {
        // Giải phóng tài nguyên nếu cần
    }
}
