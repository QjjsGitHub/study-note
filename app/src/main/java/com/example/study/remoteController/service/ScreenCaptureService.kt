package com.example.study.remoteController.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ScreenCaptureService : Service() {

    companion object {
        const val CHANNEL_ID = "ScreenCaptureChannel"
        const val NOTIFICATION_ID = 1001

        // 静态回调，用于将前台服务中生成的 MediaProjection 传回给 ViewModel
        var onServiceStarted: ((MediaProjection) -> Unit)? = null
    }

    private var mediaProjection: MediaProjection? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. 构建前台服务的常驻通知
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("局域网屏幕控制服务端")
            .setContentText("正在将您的屏幕投射到控制端...")
            .setSmallIcon(android.R.drawable.ic_menu_camera) // 替换为你项目中的图标
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // 2. 启动前台服务（Android 10+ 必须指定类型为 MEDIA_PROJECTION）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // 3. 提取 Activity 传递过来的系统授权数据
        val resultCode = intent?.getIntExtra("RESULT_CODE", -1) ?: -1
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("DATA", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra<Intent>("DATA")
        }

        // 4. 正式生成 MediaProjection
        if (resultCode != -1 && data != null) {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)

            // 5. 回调给 Activity/ViewModel 真正开始编码和网络推流
            mediaProjection?.let {
                onServiceStarted?.invoke(it)
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "屏幕捕获服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于保持后台屏幕录制运行"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaProjection?.stop()
        mediaProjection = null
        onServiceStarted = null
    }
}