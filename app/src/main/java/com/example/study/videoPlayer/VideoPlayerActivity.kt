package com.example.study.videoPlayer

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import coil.Coil
import coil.ImageLoader
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.ui.screens.VideoListScreen
import com.example.study.videoPlayer.ui.screens.VideoPlayerScreen
import com.example.study.videoPlayer.ui.theme.VideoPlayerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoPlayerActivity : ComponentActivity() {

    /** 扫描到的本地视频列表，初始为空避免 mock 数据闪烁 */
    private var videoList by mutableStateOf<List<VideoItem>>(emptyList())

    /** 是否正在扫描 */
    private var isScanning by mutableStateOf(false)

    /** 是否已完成首次扫描 */
    private var hasScanned by mutableStateOf(false)

    /** 权限请求启动器 */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                scanLocalVideos()
            } else {
                Toast.makeText(this, "需要读取权限才能扫描本地视频", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 注册 Coil ImageLoader：content:// 视频 URI → loadThumbnail()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components { add(VideoThumbnailFetcher.Factory(this@VideoPlayerActivity)) }
                .crossfade(true)
                .build()
        )

        // 自动尝试扫描
        checkPermissionAndScan()

        setContent {
            VideoPlayerTheme {
                var currentVideo by remember { mutableStateOf<VideoItem?>(null) }

                // 稳定回调引用，避免每次重组重建 lambda 导致子组件重组
                val onBack = remember {
                    {
                        currentVideo = null
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
                val onRefresh = remember { { checkPermissionAndScan() } }
                val onScan = remember { { checkPermissionAndScan() } }

                AnimatedContent(
                    targetState = currentVideo,
                    transitionSpec = {
                        if (targetState != null) {
                            // 进入播放器：从右侧滑入
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it / 3 } + fadeOut())
                        } else {
                            // 返回列表：从左侧滑入
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it / 3 } + fadeOut())
                        }
                    },
                    label = "video_navigation"
                ) { video ->
                    if (video != null) {
                        // 系统返回键 → 回到列表
                        BackHandler { currentVideo = null }

                        VideoPlayerScreen(
                            video = video,
                            onBack = onBack
                        )
                    } else {
                        VideoListScreen(
                            videos = videoList,
                            onVideoClick = { currentVideo = it },
                            onRefresh = onRefresh,
                            onScan = onScan
                        )
                    }
                }
            }
        }
    }

    /**
     * 检查权限并根据结果决定是否扫描
     * - Android 13+ (API 33): 需要 READ_MEDIA_VIDEO
     * - 低于 Android 13: 需要 READ_EXTERNAL_STORAGE
     */
    private fun checkPermissionAndScan() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // 已有权限，直接扫描
            scanLocalVideos()
        } else {
            // 申请权限
            requestPermissionLauncher.launch(permission)
        }
    }

    /**
     * 在 IO 线程扫描本地视频，不阻塞 UI
     */
    private fun scanLocalVideos() {
        if (isScanning) return
        isScanning = true

        lifecycleScope.launch {
            try {
                val videos = withContext(Dispatchers.IO) {
                    VideoScanner.scanAllVideos(contentResolver)
                }

                videoList = videos
                hasScanned = true

                val count = videos.size
                if (count > 0) {
                    Toast.makeText(
                        this@VideoPlayerActivity,
                        "已找到 $count 个本地视频",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@VideoPlayerActivity,
                        "未找到本地视频文件",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@VideoPlayerActivity,
                    "扫描失败: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isScanning = false
            }
        }
    }
}
