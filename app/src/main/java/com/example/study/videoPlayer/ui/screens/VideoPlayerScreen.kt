package com.example.study.videoPlayer.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.ui.theme.VideoBackground
import com.example.study.videoPlayer.ui.theme.VideoControlBg
import com.example.study.videoPlayer.ui.theme.VideoPrimary
import com.example.study.videoPlayer.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    video: VideoItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModelStore: ViewModelStoreOwner =
        LocalViewModelStoreOwner.current ?: return

    val viewModel: VideoPlayerViewModel =
        ViewModelProvider(viewModelStore)[VideoPlayerViewModel::class]

    // ─── 从系统读取初始亮度与音量 ───
    val activity = context as Activity
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    val originalBrightness = remember { activity.window.attributes.screenBrightness }

    // 初始化 ViewModel 的亮度/音量（仅首次）
    LaunchedEffect(Unit) {
        val curBrightness = activity.window.attributes.screenBrightness
        viewModel.updateBrightness(
            if (curBrightness < 0f) 0.5f else curBrightness.coerceIn(
                0.01f,
                1f
            )
        )
        val curVolume =
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume
        viewModel.updateVolume(curVolume)
    }

    // ── 同步 ViewModel 亮度 → 系统窗口 ──────────────────────────
    LaunchedEffect(viewModel.brightness) {
        val lp = activity.window.attributes
        lp.screenBrightness = viewModel.brightness
        activity.window.attributes = lp
    }

    // ── 同步 ViewModel 音量 → AudioManager ─────────────────────
    LaunchedEffect(viewModel.volume) {
        val streamVol = (viewModel.volume * maxVolume).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0)
    }

    // ── 自动隐藏亮度 / 音量覆盖层（滑动中重置计时器） ──────────────────────
    LaunchedEffect(viewModel.showBrightnessOverlay, viewModel.brightness) {
        if (viewModel.showBrightnessOverlay) {
            delay(1500L.milliseconds)
            viewModel.showBrightnessOverlay = false
        }
    }
    LaunchedEffect(viewModel.showVolumeOverlay, viewModel.volume) {
        if (viewModel.showVolumeOverlay) {
            delay(1500L.milliseconds)
            viewModel.showVolumeOverlay = false
        }
    }

    // ── 生命周期处理 ──────────────────────────────────────────
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ── 自由缩放与旋转状态 ──────────────────────────────────────────
    var videoScale by remember { mutableFloatStateOf(1f) }
    var videoOffset by remember { mutableStateOf(Offset.Zero) }
    var videoRotation by remember { mutableFloatStateOf(0f) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        videoScale = (videoScale * zoomChange).coerceIn(0.5f, 5f)
        videoRotation += rotationChange
        
        // 优化：只有在放大状态下（或由于旋转需要调整位置时）才允许平移
        // 且增加 0.01 的阈值防止浮点数计算误差
        if (videoScale > 1.01f || Math.abs(videoRotation) > 0.5f) {
            videoOffset += offsetChange
        } else if (videoScale <= 1.01f) {
            // 缩小或接近原始大小时，强制居中
            videoOffset = Offset.Zero
        }
    }

    // ── 组件销毁时恢复原始亮度 ──────────────────────────────────
    DisposableEffect(Unit) {
        onDispose {
            try {
                val lp = activity.window.attributes
                lp.screenBrightness = originalBrightness
                activity.window.attributes = lp
            } catch (_: Exception) {
            }
        }
    }

    // 拖拽手势的临时状态（仅手势期间使用，不放入 ViewModel）
    var dragStartBrightness by remember { mutableFloatStateOf(0f) }
    var dragStartVolume by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VideoBackground)
            .transformable(state = transformState) // 绑定缩放平移手势
            .pointerInput(Unit) {
                var accumulatedDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { _ ->
                        dragStartBrightness = viewModel.brightness
                        dragStartVolume = viewModel.volume
                        accumulatedDrag = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount

                        val halfW = size.width.toFloat() / 2f
                        val fraction = -accumulatedDrag / (size.height.toFloat() / 3f) // 适当降低调节灵敏度

                        if (change.position.x < halfW) {
                            // 左侧 — 亮度
                            viewModel.updateBrightness(
                                (dragStartBrightness + fraction).coerceIn(
                                    0.01f,
                                    1f
                                )
                            )
                            viewModel.showBrightnessOverlay = true
                        } else {
                            // 右侧 — 音量
                            viewModel.updateVolume((dragStartVolume + fraction).coerceIn(0f, 1f))
                            viewModel.showVolumeOverlay = true
                        }
                    },
                    onDragEnd = {},
                    onDragCancel = {}
                )
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { viewModel.toggleControls() }
    ) {
        // ========== 视频画面 ==========
        if (viewModel.hasError) {
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
            // 直接用 VideoItem 的宽高（扫描时已从 MediaStore 获取）
            val videoRatio = if (video.width > 0 && video.height > 0) {
                video.width.toFloat() / video.height.toFloat()
            } else {
                9f / 16f
            }

            val screenRatio = maxWidth / maxHeight
            val videoModifier = if (videoRatio >= screenRatio) {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(videoRatio)
            } else {
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(videoRatio)
            }

            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(h: SurfaceHolder) {
                                viewModel.onSurfaceReady(h.surface, video)
                            }

                            override fun surfaceChanged(
                                h: SurfaceHolder,
                                f: Int,
                                w: Int,
                                h1: Int
                            ) {
                            }

                            override fun surfaceDestroyed(h: SurfaceHolder) {
                                viewModel.onSurfaceDestroyed()
                            }
                        })
                    }
                },
                modifier = videoModifier
                    .align(Alignment.Center)
                    .graphicsLayer(
                        scaleX = videoScale,
                        scaleY = videoScale,
                        translationX = videoOffset.x,
                        translationY = videoOffset.y,
                        rotationZ = videoRotation
                    ),
            )

            // 加载中指示
            if (!viewModel.isPrepared) {
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
            visible = viewModel.showControls && !viewModel.hasError,
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
                        viewModel.togglePlayPause()
                        viewModel.showControls()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (viewModel.isPlaying) "暂停" else "播放",
                    tint = VideoBackground,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // ========== 顶部渐隐背景 ==========
        AnimatedVisibility(
            visible = viewModel.showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
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
            visible = viewModel.showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
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
                    IconButton(onClick = { }) {
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
            visible = viewModel.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Box(
                modifier = Modifier
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
            visible = viewModel.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 还原按钮 ---
                // 使用阈值判断，避免浮点数误差导致按钮无法消失
                val isTransformed = Math.abs(videoScale - 1f) > 0.01f || 
                                   Math.abs(videoRotation) > 0.5f || 
                                   videoOffset != Offset.Zero
                AnimatedVisibility(
                    visible = isTransformed,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable {
                                videoScale = 1f
                                videoOffset = Offset.Zero
                                videoRotation = 0f
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "还原画面",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "还原画面",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                ProgressBar(
                    currentPositionMs = viewModel.currentPositionMs,
                    durationMs = video.durationMs.toFloat(),
                    isPrepared = viewModel.isPrepared,
                    fallbackDurationMs = video.durationMs.toFloat(),
                    fallbackDurationText = video.formattedDuration,
                    onSeek = { seekMs ->
                        viewModel.seekTo(seekMs)
                        viewModel.showControls()
                    }
                )

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
                            onClick = { viewModel.showSpeedMenu = true },
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
                                    text = "${viewModel.playbackSpeed}x",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = viewModel.showSpeedMenu,
                            onDismissRequest = { viewModel.dismissSpeedMenu() }
                        ) {
                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${speed}x",
                                            fontWeight = if (speed == viewModel.playbackSpeed) FontWeight.Bold else FontWeight.Normal,
                                            color = if (speed == viewModel.playbackSpeed) VideoPrimary else Color.Unspecified
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSpeed(speed)
                                        viewModel.dismissSpeedMenu()
                                    }
                                )
                            }
                        }
                    }

                    // 后退10秒
                    IconButton(
                        onClick = {
                            viewModel.skipBackward()
                            viewModel.showControls()
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
                            viewModel.togglePlayPause()
                            viewModel.showControls()
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(VideoPrimary)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (viewModel.isPlaying) "暂停" else "播放",
                            tint = Color(0xFF003544),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 前进10秒
                    IconButton(
                        onClick = {
                            viewModel.skipForward()
                            viewModel.showControls()
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
                            val isLandscape =
                                context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                            context.requestedOrientation = if (isLandscape) {
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
            }
        }

        // ========== 手势提示覆盖层 ==========
        // 左侧 - 亮度调节
        AnimatedVisibility(
            visible = viewModel.showBrightnessOverlay,
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
                    text = "${(viewModel.brightness * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // 右侧 - 音量调节
        AnimatedVisibility(
            visible = viewModel.showVolumeOverlay,
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
                    text = "${(viewModel.volume * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(
    currentPositionMs: Float,
    durationMs: Float,
    isPrepared: Boolean,
    fallbackDurationMs: Float,
    fallbackDurationText: String,
    onSeek: (seekMs: Int) -> Unit,
) {
    val totalMs = if (isPrepared && durationMs > 0) durationMs else fallbackDurationMs
    val progress = if (totalMs > 0) (currentPositionMs / totalMs).coerceIn(0f, 1f) else 0f
    var isDragging by remember { mutableStateOf(false) }
    var sliderProgress by remember { mutableFloatStateOf(progress) }

    LaunchedEffect(progress, isDragging) {
        if (!isDragging) {
            sliderProgress = progress
        }
    }

    val displayedPositionMs = if (isDragging) {
        sliderProgress * totalMs
    } else {
        currentPositionMs
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatTime(displayedPositionMs.roundToLong()),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.Center
        )
        Slider(
            value = sliderProgress,
            onValueChange = { newProgress ->
                isDragging = true
                sliderProgress = newProgress
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek((sliderProgress * totalMs).roundToLong().toInt())
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
                fallbackDurationText
            },
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.Center
        )
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
