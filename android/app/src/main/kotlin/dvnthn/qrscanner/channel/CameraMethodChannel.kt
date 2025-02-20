import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object CameraMethodChannel {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    fun startCamera(context: Context, onQrCodeDetected: (String) -> Unit) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val barcodeScanner = BarcodeScanning.getClient()

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    val buffer = imageProxy.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val mediaImage = imageProxy.imageInfo.rotationDegrees


                    val image = InputImage.fromByteArray(
                        bytes,
                        imageProxy.width,
                        imageProxy.height,
                        rotationDegrees,
                        InputImage.IMAGE_FORMAT_YV12
                    )
                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                barcode.rawValue?.let { onQrCodeDetected(it) }
                            }
                        }
                        .addOnCompleteListener { imageProxy.close() }

                }
            }

            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, imageAnalyzer)
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        if (::cameraProviderFuture.isInitialized) {
            cameraProviderFuture.get().unbindAll()
            cameraExecutor.shutdown()
        }
    }
}