
package com.dvnthn.qrscanner.channel

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import dvnthn.qrscanner.channel.CameraPreviewView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class CameraPreviewFactory(
    private val messenger: BinaryMessenger,
    private val lifecycleOwner: LifecycleOwner
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, id: Int, args: Any?): PlatformView {
        return CameraPreviewView(context, lifecycleOwner, id, messenger)
    }
}
