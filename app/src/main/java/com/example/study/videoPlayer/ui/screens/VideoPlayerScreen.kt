package com.example.study.videoPlayer.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.ui.theme.VideoAccent
import com.example.study.videoPlayer.ui.theme.VideoBackground
import com.example.study.videoPlayer.ui.theme.VideoControlBg
import com.example.study.videoPlayer.ui.theme.VideoOnSurfaceVariant
import com.example.study.videoPlayer.ui.theme.VideoPrimary
import kotlinx.coroutines.delay
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    video: VideoItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableFloatStateOf(0f) }
    var showControls by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var showVolumeOverlay by remember { mutableStateOf(false) }
    var showBrightnessOverlay by remember { mutableStateOf(true) }
    var volume by remember { mutableFloatStateOf(0.7f) }
    var brightness by remember { mutableFloatStateOf(0.7f) }

    var isPrepared by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var durationMs by remember { mutableFloatStateOf(video.durationMs.toFloat()) }

    var videoWidth by remember { mutableIntStateOf(0) }
    var videoHeight by remember { mutableIntStateOf(0) }
    var surfaceReady by remember { mutableStateOf(false) }
    var currentSurface by remember { mutableStateOf<Surface?>(null) }

    // 创建 MediaPlayer 实例
    val mediaPlayer = remember { MediaPlayer() }

    // ========== 准备播放器 ==========
    LaunchedEffect(surfaceReady, video) {
        if (!surfaceReady) return@LaunchedEffect

        try {
            mediaPlayer.reset()
            isPrepared = false
            hasError = false

            // 优先使用 content URI，其次用文件路径
            val contentUri = video.thumbnailPath?.let { Uri.parse(it) }
            if (contentUri != null && contentUri.scheme == "content") {
                mediaPlayer.setDataSource(context, contentUri)
            } else {
                mediaPlayer.setDataSource(video.filePath)
            }

            mediaPlayer.setSurface(currentSurface)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener { mp ->
                durationMs = mp.duration.toFloat()
                isPrepared = true
                mp.start()
                isPlaying = true
                if (playbackSpeed != 1.0f) {
                    mp.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
                }
            }

            mediaPlayer.setOnVideoSizeChangedListener { _, width, height ->
                videoWidth = width
                videoHeight = height
            }

            mediaPlayer.setOnErrorListener { _, _, _ ->
                hasError = true
                isPrepared = false
                true
            }

            mediaPlayer.setOnCompletionListener {
                isPlaying = false
                currentPositionMs = durationMs
            }
        } catch (_: Exception) {
            hasError = true
        }
    }

    // ========== 轮询播放进度 ==========
    LaunchedEffect(isPlaying, isPrepared) {
        while (isPlaying && isPrepared) {
            try {
                currentPositionMs = mediaPlayer.currentPosition.toFloat()
            } catch (_: Exception) { }
            delay(1000L)
        }
    }

    // ========== 生命周期处理 ==========
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    mediaPlayer.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ========== 组件销毁时释放 ==========
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.release()
            } catch (_: Exception) { }
        }
    }

    // ========== 总时长优先取真实时长 ==========
    val totalMs = if (isPrepared && durationMs > 0) durationMs else video.durationMs.toFloat()
    val progress = if (totalMs > 0) (currentPositionMs / totalMs).coerceIn(0f, 1f) else 0f
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    // 自动隐藏控件
    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            delay(4000L)
            showControls = false
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VideoBackground)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showControls = !showControls
            }
    ) {
        // ========== 视频画面 ==========
        if (hasError) {
            // 播放失败：占位提示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = video.title,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "无法播放此视频",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // 根据视频原始宽高计算比例，未获取到时回退 16:9
            val videoRatio = if (videoWidth > 0 && videoHeight > 0) {
                videoWidth.toFloat() / videoHeight.toFloat()
            } else {
                16f / 9f
            }

            // 视频宽于屏幕则按宽度约束，高于屏幕则按高度约束，始终完整可见无裁切
            val screenRatio = maxWidth / maxHeight
            val videoModifier = if (videoRatio >= screenRatio) {
                Modifier.fillMaxWidth().aspectRatio(videoRatio)
            } else {
                Modifier.fillMaxHeight().aspectRatio(videoRatio)
            }

            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(
                                st: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {
                                currentSurface = Surface(st)
                                surfaceReady = true
                            }

                            override fun onSurfaceTextureSizeChanged(
                                st: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {}

                            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                                currentSurface?.release()
                                currentSurface = null
                                surfaceReady = false
                                return true
                            }

                            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                        }
                    }
                },
                modifier = videoModifier.align(Alignment.Center),
            )

            // 加载中指示
            if (!isPrepared) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = video.title,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${video.resolution} · ${video.formattedSize}",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 中央大播放/暂停按钮（暂停或控件隐藏时显示）
        AnimatedVisibility(
            visible = showControls && !hasError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .clickable {
                        if (isPrepared) {
                            if (isPlaying) {
                                mediaPlayer.pause()
                            } else {
                                mediaPlayer.start()
                            }
                            isPlaying = !isPlaying
                        }
                        showControls = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = VideoBackground,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // ========== 顶部渐隐背景 ==========
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // ========== 顶部控制栏 ==========
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TopAppBar(
                modifier = Modifier.padding(statusBarPadding),
                title = {
                    Column {
                        Text(
                            text = video.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            color = Color.White
                        )
                        Text(
                            text = video.resolution,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 更多操作 */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }

        // ========== 底部渐隐背景 ==========
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }

        // ========== 底部控制栏 ==========
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(navBarPadding)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                // --- 进度条 + 时间 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentPositionMs.roundToLong()),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.width(44.dp),
                        textAlign = TextAlign.Center
                    )
                    Slider(
                        value = progress,
                        onValueChange = { newProgress ->
                            val seekMs = (newProgress * totalMs).roundToLong().toInt()
                            if (isPrepared) {
                                mediaPlayer.seekTo(seekMs)
                            }
                            currentPositionMs = seekMs.toFloat()
                            showControls = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = VideoPrimary,
                            activeTrackColor = VideoPrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        text = if (isPrepared && durationMs > 0) {
                            formatTime(durationMs.roundToLong())
                        } else {
                            video.formattedDuration
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.width(44.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- 播放控制按钮行 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 播放速度
                    Box {
                        IconButton(
                            onClick = { showSpeedMenu = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "播放速度",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "${playbackSpeed}x",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false }
                        ) {
                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${speed}x",
                                            fontWeight = if (speed == playbackSpeed) FontWeight.Bold else FontWeight.Normal,
                                            color = if (speed == playbackSpeed) VideoPrimary else Color.Unspecified
                                        )
                                    },
                                    onClick = {
                                        playbackSpeed = speed
                                        if (isPrepared) {
                                            mediaPlayer.playbackParams =
                                                PlaybackParams().setSpeed(speed)
                                        }
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 后退10秒
                    IconButton(
                        onClick = {
                            if (isPrepared) {
                                val newPos =
                                    (mediaPlayer.currentPosition - 10_000).coerceAtLeast(0)
                                mediaPlayer.seekTo(newPos)
                                currentPositionMs = newPos.toFloat()
                            }
                            showControls = true
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "后退10秒",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "10",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 9.sp
                            )
                        }
                    }

                    // 播放/暂停
                    IconButton(
                        onClick = {
                            if (isPrepared) {
                                if (isPlaying) {
                                    mediaPlayer.pause()
                                } else {
                                    mediaPlayer.start()
                                }
                                isPlaying = !isPlaying
                            }
                            showControls = true
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(VideoPrimary)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = Color(0xFF003544),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 前进10秒
                    IconButton(
                        onClick = {
                            if (isPrepared) {
                                val newPos = (mediaPlayer.currentPosition + 10_000)
                                    .coerceAtMost(totalMs.toInt())
                                mediaPlayer.seekTo(newPos)
                                currentPositionMs = newPos.toFloat()
                            }
                            showControls = true
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "前进10秒",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "10",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 9.sp
                            )
                        }
                    }

                    // 全屏/旋转
                    IconButton(
                        onClick = {
                            val activity = context as Activity
                            val isLandscape = context.resources.configuration.orientation== Configuration.ORIENTATION_LANDSCAPE
                            activity.requestedOrientation = if (isLandscape) {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "全屏",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 缓冲进度指示（仅装饰）
                LinearProgressIndicator(
                    progress = { 0.85f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(2.dp)
                        .alpha(0.3f),
                    color = VideoPrimary.copy(alpha = 0.5f),
                    trackColor = Color.Transparent
                )
            }
        }

        // ========== 手势提示覆盖层 ==========
        // 左侧 - 亮度调节
        AnimatedVisibility(
            visible = showBrightnessOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(VideoControlBg)
                    .padding(12.dp)
            ) {
                Text(text = "☀️", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "亮度",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "${(brightness * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // 右侧 - 音量调节
        AnimatedVisibility(
            visible = showVolumeOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(VideoControlBg)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "音量",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "${(volume * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * 格式化毫秒为 mm:ss 或 hh:mm:ss
 */
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
