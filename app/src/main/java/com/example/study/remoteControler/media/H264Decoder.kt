package com.example.study.remoteControler.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import java.nio.ByteBuffer

class H264Decoder(private val surface: Surface, width: Int = 720, height: Int = 1280) {
    private var mediaCodec: MediaCodec? = null

    init {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
            mediaCodec?.configure(format, surface, null, 0)
            mediaCodec?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 将收到的网络字节流塞入解码器
    fun decodeFrame(frameData: ByteArray) {
        val codec = mediaCodec ?: return
        try {
            val inputBufferIndex = codec.dequeueInputBuffer(10000)
            if (inputBufferIndex >= 0) {
                val inputBuffer: ByteBuffer? = codec.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(frameData)
                codec.queueInputBuffer(inputBufferIndex, 0, frameData.size, System.currentTimeMillis(), 0)
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            while (outputBufferIndex >= 0) {
                // true 表示将解码后的画面渲染到配置的 Surface 上
                codec.releaseOutputBuffer(outputBufferIndex, true)
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}