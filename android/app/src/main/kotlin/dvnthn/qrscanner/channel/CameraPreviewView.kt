// CameraPreviewView.kt
package dvnthn.qrscanner.channel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import io.flutter.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreviewView(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    id: Int,
    messenger: BinaryMessenger
) : PlatformView {

    private val textureView: TextureView = TextureView(context)

    private val previewView: PreviewView = PreviewView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private var initialized = false
    private val channel: MethodChannel =
        MethodChannel(messenger, "dvnthn.qrscanner/camera_view_$id")

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1001
    }

    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var lastQRValue: String? = null
    private var lastSentTime: Long = 0
    private val DEBOUNCE_TIME_MS = 1000

    init {
        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "initialize" -> {
                    if (!checkCameraPermission()) {
                        Log.e(
                            "CameraPreview",
                            "Camera permission not granted. Requesting permission..."
                        )
                        if (context is Activity) {
                            ActivityCompat.requestPermissions(
                                context,
                                arrayOf(Manifest.permission.CAMERA),
                                REQUEST_CAMERA_PERMISSION
                            )
                        }
                        result.error(
                            "PERMISSION_NOT_GRANTED",
                            "Camera permission not granted",
                            null
                        )
                        return@setMethodCallHandler
                    } else {
                        initializeCamera()
                        result.success("Initialized")
                    }
                }

                else -> result.notImplemented()
            }
        }
    }


    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun initializeCamera() {
        if (!initialized) {

            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    // Lấy ProcessCameraProvider một cách an toàn
                    val cameraProvider = cameraProviderFuture.get()

                    // Tạo preview và gán surfaceProvider cho PreviewView
                    val preview = Preview.Builder().build().also {
                        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Chỉ xử lý khung hình mới nhất
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888) // Tối ưu định dạng ảnh
                        .build().also { analysis ->
                            analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                processImage(imageProxy)
                            }
                        }


                    // Chọn camera sau (BACK)
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    cameraProvider.unbindAll()
                    // Bind preview vào lifecycle của Activity
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                    initialized = true
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Error initializing camera: ${e.message}", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient(options)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val currentTime = System.currentTimeMillis()
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { qrValue ->
                            if (qrValue != lastQRValue && (currentTime - lastSentTime) > DEBOUNCE_TIME_MS) {
                                lastQRValue = qrValue
                                lastSentTime = currentTime
                                val boundingBox = barcode.boundingBox
                                val qrData = mapOf(
                                    "qrValue" to qrValue,
                                    "boundingBox" to mapOf(
                                        "left" to (boundingBox?.left ?: 0),
                                        "top" to (boundingBox?.top ?: 0),
                                        "right" to (boundingBox?.right ?: 0),
                                        "bottom" to (boundingBox?.bottom ?: 0),
                                        "width" to (boundingBox?.width() ?: 0),
                                        "height" to (boundingBox?.height() ?: 0),
                                    ),
                                    "cameraWidth" to imageProxy.width,
                                    "cameraHeight" to imageProxy.height,
                                )
                                Log.i("QRScanner", "QR Code detected: $qrData")
                                channel.invokeMethod("onQRCodeDetected", qrData)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QRScanner", "Error scanning QR code: ${e.message}", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }


    override fun getView(): View = previewView

    override fun dispose() {
        // Giải phóng tài nguyên nếu cần
    }
}
