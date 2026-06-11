package com.example.study.videoPlayer

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.Coil
import coil.ImageLoader
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.ui.screens.VideoListScreen
import com.example.study.videoPlayer.ui.screens.VideoPlayerScreen
import com.example.study.videoPlayer.ui.theme.VideoPlayerTheme
import com.example.study.videoPlayer.viewmodel.VideoListViewModel

class VideoPlayerActivity : ComponentActivity() {

    /** 权限授予后递增，通知 composable 触发扫描 */
    private var permissionGrantedTrigger by mutableIntStateOf(0)

    /** 权限请求启动器 */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                permissionGrantedTrigger++
            } else {
                Toast.makeText(this, "需要读取权限才能扫描本地视频", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 注册 Coil ImageLoader：content:// 视频 URI → loadThumbnail()
        Coil.setImageLoader(
            ImageLoader.Builder(applicationContext)
                .components { add(VideoThumbnailFetcher.Factory(applicationContext)) }
                .crossfade(false)
                .build()
        )

        setContent {
            VideoPlayerTheme {

                val listViewModel: VideoListViewModel by viewModels()

                var currentVideo by remember { mutableStateOf<VideoItem?>(null) }

                // ── 控制状态栏显示/隐藏 ──────────────────────
                LaunchedEffect(currentVideo) {
                    val window = this@VideoPlayerActivity.window
                    val controller = WindowCompat.getInsetsController(window, window.decorView)
                    if (currentVideo != null) {
                        // 播放界面：隐藏状态栏
                        controller.hide(WindowInsetsCompat.Type.statusBars())
                        // 可选：设置隐藏行为（如滑动显示后自动隐藏）
                        controller.systemBarsBehavior = 
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        // 列表界面：显示状态栏
                        controller.show(WindowInsetsCompat.Type.statusBars())
                    }
                }

                // ── 观察 ViewModel 一次性事件 ──────────────────────
                LaunchedEffect(Unit) {
                    listViewModel.events.collect { event ->
                        when (event) {
                            is VideoListViewModel.ScanEvent.Success -> {
                                val count = event.count
                                val msg =
                                    if (count > 0) "已找到 $count 个本地视频" else "未找到本地视频文件"
                                Toast.makeText(this@VideoPlayerActivity, msg, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is VideoListViewModel.ScanEvent.Error -> {
                                Toast.makeText(
                                    this@VideoPlayerActivity,
                                    "扫描失败: ${event.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                // ── 首次进入自动扫描（权限已授予时） ──────────────
                LaunchedEffect(Unit) {
                    val permission = getStoragePermission()
                    if (ContextCompat.checkSelfPermission(
                            this@VideoPlayerActivity, permission
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        listViewModel.scanVideos()
                    } else {
                        requestPermissionLauncher.launch(permission)
                    }
                }

                // ── 用户授予权限后触发扫描 ────────────────────────
                LaunchedEffect(permissionGrantedTrigger) {
                    if (permissionGrantedTrigger > 0) {
                        listViewModel.scanVideos()
                    }
                }

                // 稳定回调引用
                val onBack = remember {
                    {
                        currentVideo = null
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
                val onRefresh = remember { { listViewModel.scanVideos() } }
                val onScan = remember {
                    {
                        val permission = getStoragePermission()
                        if (ContextCompat.checkSelfPermission(
                                this@VideoPlayerActivity, permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            listViewModel.scanVideos()
                        } else {
                            requestPermissionLauncher.launch(permission)
                        }
                    }
                }

                AnimatedContent(
                    targetState = currentVideo,
                    transitionSpec = {
                        if (targetState != null) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it / 3 } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it / 3 } + fadeOut())
                        }
                    },
                    label = "video_navigation"
                ) { video ->
                    if (video != null) {
                        BackHandler(onBack = onBack)

                        VideoPlayerScreen(
                            video = video,
                            onBack = onBack
                        )
                    } else {
                        VideoListScreen(
                            viewModel = listViewModel,
                            onVideoClick = { currentVideo = it },
                            onRefresh = onRefresh,
                            onScan = onScan
                        )
                    }
                }
            }
        }
    }

    private fun getStoragePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}
