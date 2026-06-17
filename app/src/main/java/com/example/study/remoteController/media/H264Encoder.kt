package com.example.study.remoteController.media

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.view.Surface
import kotlin.concurrent.thread

class H264Encoder(
    private val mediaProjection: MediaProjection,
    private val onFrameEncoded: (ByteArray) -> Unit,
    private val width: Int = 720,
    private val height: Int = 1280
) {
    private var mediaCodec: MediaCodec? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var inputSurface: Surface? = null
    private var isEncoding = false

    fun start() {
        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000) // 2Mbps 码率，局域网可以适当调高
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30) // 30帧
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // 关键帧间隔 1s

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = mediaCodec?.createInputSurface()
            mediaCodec?.start()

            // 将录屏输出直接绑定到编码器的输入 Surface
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture", width, height, 1,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                inputSurface, null, null
            )

            isEncoding = true
            startEncodeThread()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun startEncodeThread() {
        thread {
            val codec = mediaCodec ?: return@thread
            val bufferInfo = MediaCodec.BufferInfo()
            while (isEncoding) {
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                    val outData = ByteArray(bufferInfo.size)
                    outputBuffer?.get(outData)

                    // 将 H.264 帧数据回调给网络层发送
                    onFrameEncoded(outData)

                    codec.releaseOutputBuffer(outputBufferIndex, false)
                }
            }
        }
    }
}