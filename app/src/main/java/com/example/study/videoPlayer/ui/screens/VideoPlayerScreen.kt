package com.example.study.videoPlayer.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessHigh
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.study.R
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.ui.theme.VideoBackground
import com.example.study.videoPlayer.ui.theme.VideoControlBg
import com.example.study.videoPlayer.ui.theme.VideoPrimary
import com.example.study.videoPlayer.util.formatTime
import com.example.study.videoPlayer.viewmodel.VideoPlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    viewModel: VideoPlayerViewModel, video: VideoItem, onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val activity: Activity
    try {
        activity = context as Activity
    } catch (_: Exception) {
        return
    }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    val originalBrightness = remember { activity.window.attributes.screenBrightness }

    // ── 亮度/音量初始化 + 持续同步（异步处理） ──
    LaunchedEffect(Unit) {
        delay(300.milliseconds)

        val (initBrightness, initVolume) = withContext(Dispatchers.Default) {
            val b = activity.window.attributes.screenBrightness
            val v = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume
            b to v
        }

        viewModel.updateBrightness(
            if (initBrightness < 0f) 0.5f else initBrightness.coerceIn(
                0.01f, 1f
            )
        )
        viewModel.updateVolume(initVolume)

        launch(Dispatchers.Default) {
            snapshotFlow { viewModel.brightness }.conflate().collect { value ->
                withContext(Dispatchers.Main) {
                    val lp = activity.window.attributes
                    lp.screenBrightness = value
                    activity.window.attributes = lp
                }
                delay(40.milliseconds)
            }
        }

        launch(Dispatchers.IO) {
            snapshotFlow { viewModel.volume }.collect { value ->
                val streamVol = (value * maxVolume).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0)
            }
        }
    }

    // 控制栏入场延迟：避免与转场动画竞争资源
    var isScreenReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400.milliseconds)
        isScreenReady = true
    }

    // ── 自动隐藏调节覆盖层 ──────────────────────
    LaunchedEffect(viewModel.showBrightnessOverlay) {
        if (viewModel.showBrightnessOverlay) {
            delay(1500L.milliseconds)
            viewModel.dismissBrightnessOverlay()
        }
    }
    LaunchedEffect(viewModel.showVolumeOverlay) {
        if (viewModel.showVolumeOverlay) {
            delay(1500L.milliseconds)
            viewModel.dismissVolumeOverlay()
        }
    }

    // ── 生命周期处理 ──────────────────────────────────────────
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) viewModel.onPause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── 自由缩放与旋转状态 ──────────────────────────────────────────
    var videoScale by remember { mutableFloatStateOf(1f) }
    var videoOffset by remember { mutableStateOf(Offset.Zero) }
    var videoRotation by remember { mutableFloatStateOf(0f) }

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

    // ── 视频比例计算 ──
    val videoRatio = remember(video.width, video.height) {
        if (video.width > 0 && video.height > 0) video.width.toFloat() / video.height.toFloat() else 16f / 9f
    }

    // 实时计算视频画面边界，解决横竖屏切换时闭包捕获导致边界失效的问题
    val videoLayout = remember(videoRatio, windowInfo.containerSize) {
        val size = windowInfo.containerSize
        val screenWidthPx = size.width.toFloat()
        val screenHeightPx = size.height.toFloat()
        val screenRatio = screenWidthPx / screenHeightPx

        val vWidthPx: Float
        val vHeightPx: Float
        if (videoRatio > screenRatio) {
            vWidthPx = screenWidthPx
            vHeightPx = screenWidthPx / videoRatio
        } else {
            vHeightPx = screenHeightPx
            vWidthPx = screenHeightPx * videoRatio
        }
        val topPx = (screenHeightPx - vHeightPx) / 2
        val bottomPx = (screenHeightPx + vHeightPx) / 2

        // 将精确的像素值转换为 Dp 供渲染层使用
        val vWidthDp = with(density) { vWidthPx.toDp() }
        val vHeightDp = with(density) { vHeightPx.toDp() }

        Triple(vWidthDp, vHeightDp, topPx..bottomPx)
    }
    val (vWidthDp, vHeightDp, videoYRange) = videoLayout


    Box(modifier = Modifier
        .fillMaxSize()
        .background(VideoBackground)
        .pointerInput(Unit) {
            detectTapGestures(onTap = { viewModel.toggleControls() })
        }
        .pointerInput(videoLayout) {
            detectTransformGestures { centroid, pan, zoom, rotation ->

                // 判断触摸点是否在视频画面内
                val isInsideVideoY = centroid.y in videoYRange


                // 2. 使用阈值判断，避免浮点数精度问题导致的“状态粘连”
                val isScaling = abs(zoom - 1f) > 0.001f
                val isRotating = abs(rotation) > 0.1f
                val isMultiTouch = isScaling || isRotating

                val isAlreadyTransformed =
                    abs(videoScale - 1f) > 0.01f || abs(videoRotation) > 0.5f || videoOffset.getDistance() > 1f

                if (isMultiTouch || isAlreadyTransformed) {
                    // 变换模式：允许全局操作，确保放大后边缘区域也能“抓得住”
                    videoScale = (videoScale * zoom).coerceIn(0.5f, 5f)
                    videoRotation += rotation
                    videoOffset += pan
                } else if (isInsideVideoY) {
                    // 调节模式：仅在视频垂直高度范围内生效，避开顶部/底部黑边（控制栏）区域
                    val fraction = -pan.y / (size.height.toFloat() / 3f)
                    if (centroid.x < size.width / 2) {
                        viewModel.updateBrightness(
                            (viewModel.brightness + fraction).coerceIn(
                                0.01f, 1f
                            )
                        )
                        viewModel.showBrightnessAdjusting()
                    } else {
                        viewModel.updateVolume((viewModel.volume + fraction).coerceIn(0f, 1f))
                        viewModel.showVolumeAdjusting()
                    }
                }
            }
        }) {
        // ========== 视频画面 ==========
        if (viewModel.hasError) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        text = stringResource(R.string.video_play_error),
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            override fun onSurfaceTextureAvailable(
                                st: SurfaceTexture, w: Int, h: Int
                            ) {
                                viewModel.onSurfaceReady(Surface(st), video)
                            }

                            override fun onSurfaceTextureSizeChanged(
                                st: SurfaceTexture, w: Int, h: Int
                            ) {
                            }

                            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                                viewModel.onSurfaceDestroyed()
                                return true
                            }

                            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(vWidthDp, vHeightDp)
                    .graphicsLayer {
                        scaleX = videoScale
                        scaleY = videoScale
                        translationX = videoOffset.x
                        translationY = videoOffset.y
                        rotationZ = videoRotation
                    },
            )
        }

        // 中央大播放/暂停按钮 (优化：移除 Box 嵌套，直接对 Icon 使用 padding 模拟背景)
        AnimatedVisibility(
            visible = isScreenReady && viewModel.showControls && !viewModel.hasError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Icon(
                imageVector = if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (viewModel.isPlaying) stringResource(R.string.video_pause) else stringResource(
                    R.string.video_play
                ),
                tint = VideoBackground,
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        viewModel.togglePlayPause()
                        viewModel.showControls()
                    }
                    .padding(18.dp))
        }

        // ========== 顶部渐隐背景 ==========
        AnimatedVisibility(
            visible = isScreenReady && viewModel.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        ) {
            TopAppBar(
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
            }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.video_back),
                        tint = Color.White
                    )
                }
            }, actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.video_more),
                        tint = Color.White
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent, titleContentColor = Color.White
            )
            )
        }

        // ========== 底部渐隐背景 ==========
        AnimatedVisibility(
            visible = isScreenReady && viewModel.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isTransformed by remember {
                        derivedStateOf {
                            abs(videoScale - 1f) > 0.01f || abs(videoRotation) > 0.5f || videoOffset.getDistance() > 1f
                        }
                    }
                    AnimatedVisibility(
                        visible = isTransformed, enter = fadeIn(), exit = fadeOut()
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
                            contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.video_reset_hint),
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.video_reset_hint),
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
                        })

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box {
                            IconButton(
                                onClick = { viewModel.showSpeedMenu = true },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Speed,
                                        contentDescription = stringResource(R.string.video_playback_speed),
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
                                onDismissRequest = { viewModel.dismissSpeedMenu() }) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = {
                                        Text(
                                            text = "${speed}x",
                                            fontWeight = if (speed == viewModel.playbackSpeed) FontWeight.Bold else FontWeight.Normal,
                                            color = if (speed == viewModel.playbackSpeed) VideoPrimary else Color.Unspecified
                                        )
                                    },
                                        onClick = { viewModel.setSpeed(speed); viewModel.dismissSpeedMenu() })
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.skipBackward(); viewModel.showControls() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.FastRewind,
                                    contentDescription = stringResource(R.string.video_rewind),
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = stringResource(R.string.video_seconds),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.togglePlayPause(); viewModel.showControls() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(VideoPrimary)
                        ) {
                            Icon(
                                imageVector = if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF003544),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.skipForward(); viewModel.showControls() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.FastForward,
                                    contentDescription = stringResource(R.string.video_forward),
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = stringResource(R.string.video_seconds),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val isLandscape =
                                    context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                                activity.requestedOrientation =
                                    if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }, modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = stringResource(R.string.video_fullscreen),
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // ========== 手势提示覆盖层 ==========
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
                Icon(
                    Icons.Default.BrightnessHigh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.video_brightness),
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
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.video_volume),
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
    var sliderProgress by remember { mutableFloatStateOf(0f) }

    if (!isDragging) {
        sliderProgress = progress
    }

    val displayedPositionMs = if (isDragging) sliderProgress * totalMs else currentPositionMs

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = formatTime(displayedPositionMs.roundToLong()),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.Center
        )
        Slider(
            value = sliderProgress,
            onValueChange = { isDragging = true; sliderProgress = it },
            onValueChangeFinished = {
                isDragging = false; onSeek((sliderProgress * totalMs).roundToLong().toInt())
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
            text = if (isPrepared && durationMs > 0) formatTime(durationMs.roundToLong()) else fallbackDurationText,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.Center
        )
    }
}
