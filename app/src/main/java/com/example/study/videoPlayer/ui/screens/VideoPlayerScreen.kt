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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalConfiguration
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

    val activity = context as? Activity ?: return

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    val originalBrightness = remember { activity.window.attributes.screenBrightness }

    // 控制栏入场延迟：避免与转场动画竞争资源
    var isScreenReady by remember { mutableStateOf(false) }

    // ── 亮度/音量初始化 + 持续同步（异步处理） ──
    LaunchedEffect(Unit) {
        delay(400.milliseconds)
        isScreenReady = true

        val (initBrightness, initVolume) = withContext(Dispatchers.Default) {
            val b = activity.window.attributes.screenBrightness
            val v = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume
            b to v
        }

        viewModel.updateBrightness(
            if (initBrightness < 0f) 0.5f else initBrightness.coerceIn(
                0.01f,
                1f
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

    val videoLayout by remember(videoRatio) {
        derivedStateOf {
            val size = windowInfo.containerSize
            val screenWidthPx = size.width.toFloat()
            val screenHeightPx = size.height.toFloat()
            if (screenWidthPx == 0f || screenHeightPx == 0f) {
                return@derivedStateOf Triple(0.dp, 0.dp, 0f..0f)
            }

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

            val vWidthDp = with(density) { vWidthPx.toDp() }
            val vHeightDp = with(density) { vHeightPx.toDp() }

            Triple(vWidthDp, vHeightDp, topPx..bottomPx)
        }
    }
    val (vWidthDp, vHeightDp, videoYRange) = videoLayout

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VideoBackground)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { viewModel.toggleControls() })
            }
            .pointerInput(videoLayout) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    val isInsideVideoY = centroid.y in videoYRange
                    val isScaling = abs(zoom - 1f) > 0.001f
                    val isRotating = abs(rotation) > 0.1f
                    val isMultiTouch = isScaling || isRotating
                    val isAlreadyTransformed =
                        abs(videoScale - 1f) > 0.01f || abs(videoRotation) > 0.5f || videoOffset.getDistance() > 1f

                    if (isMultiTouch || isAlreadyTransformed) {
                        videoScale = (videoScale * zoom).coerceIn(0.5f, 5f)
                        videoRotation += rotation
                        videoOffset += pan
                    } else if (isInsideVideoY) {
                        val fraction = -pan.y / (size.height.toFloat() / 3f)
                        if (centroid.x < size.width / 2) {
                            viewModel.updateBrightness(
                                (viewModel.brightness + fraction).coerceIn(
                                    0.01f,
                                    1f
                                )
                            )
                            viewModel.showBrightnessAdjusting()
                        } else {
                            viewModel.updateVolume(
                                (viewModel.volume + fraction).coerceIn(
                                    0f,
                                    1f
                                )
                            )
                            viewModel.showVolumeAdjusting()
                        }
                    }
                }
            }
    ) {
        if (viewModel.hasError) {
            VideoErrorView(video.title)
        } else {
            VideoSurface(
                vWidthDp = vWidthDp,
                vHeightDp = vHeightDp,
                videoScale = videoScale,
                videoOffset = videoOffset,
                videoRotation = videoRotation,
                onSurfaceReady = { surface -> viewModel.onSurfaceReady(surface, video) },
                onSurfaceDestroyed = { viewModel.onSurfaceDestroyed() }
            )
        }

        // ========== 2. 静态 UI 控件层 ==========
        // 包含顶部标题栏、底部控制条、中央播放按钮。这些组件坐标固定，不随视频缩放。
        VideoControlLayer(
            viewModel = viewModel,
            video = video,
            activity = activity,
            isScreenReady = isScreenReady,
            onBack = onBack,
            isTransformed = abs(videoScale - 1f) > 0.01f || abs(videoRotation) > 0.5f || videoOffset.getDistance() > 1f,
            onResetTransform = {
                videoScale = 1f
                videoOffset = Offset.Zero
                videoRotation = 0f
            }
        )

        // ========== 3. 手势反馈提示层 ==========
        // 亮度、音量调节时的图标显示
        VideoOverlayLayer(viewModel = viewModel)
    }
}

/**
 * 核心视频表面组件
 */
@Composable
private fun BoxScope.VideoSurface(
    vWidthDp: androidx.compose.ui.unit.Dp,
    vHeightDp: androidx.compose.ui.unit.Dp,
    videoScale: Float,
    videoOffset: Offset,
    videoRotation: Float,
    onSurfaceReady: (Surface) -> Unit,
    onSurfaceDestroyed: () -> Unit
) {
    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                        onSurfaceReady(Surface(st))
                    }

                    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
                    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                        onSurfaceDestroyed()
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

/**
 * 视频播放错误显示
 */
@Composable
private fun VideoErrorView(videoTitle: String) {
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
                text = videoTitle,
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
}

/**
 * UI 控制图层（不随视频缩放变换）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.VideoControlLayer(
    viewModel: VideoPlayerViewModel,
    video: VideoItem,
    activity: Activity,
    isScreenReady: Boolean,
    onBack: () -> Unit,
    isTransformed: Boolean,
    onResetTransform: () -> Unit
) {
    val configuration = LocalConfiguration.current

    // 中央大播放/暂停按钮
    AnimatedVisibility(
        visible = isScreenReady && viewModel.showControls && !viewModel.hasError,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center),
    ) {
        Icon(
            imageVector = if (viewModel.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = null,
            tint = VideoBackground,
            modifier = Modifier
                .size(72.dp)
                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                .clip(CircleShape)
                .clickable {
                    viewModel.togglePlayPause()
                    viewModel.showControls()
                }
                .padding(18.dp)
        )
    }

    // 顶部标题栏
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
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            ), modifier = Modifier
        )
    }

    // 底部控制栏
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 重置按钮（仅在画面发生变换时显示）
                if (isTransformed) {
                    Row(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onResetTransform() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
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

                ProgressBar(
                    currentPositionMs = viewModel.currentPositionMs,
                    durationMs = video.durationMs.toFloat(),
                    isPrepared = viewModel.isPrepared,
                    fallbackDurationMs = video.durationMs.toFloat(),
                    fallbackDurationText = video.formattedDuration,
                    onSeek = { viewModel.seekTo(it); viewModel.showControls() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    // 倍速菜单
                    IconButton(
                        onClick = { viewModel.showSpeedMenu = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${viewModel.playbackSpeed}x",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
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
                                    onClick = { viewModel.setSpeed(speed); viewModel.dismissSpeedMenu() }
                                )
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
                                contentDescription = null,
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
                                contentDescription = null,
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

                    IconButton(onClick = {
                        val isLandscape =
                            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                        activity.requestedOrientation =
                            if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 手势调节反馈层（亮度/音量指示器）
 */
@Composable
private fun BoxScope.VideoOverlayLayer(viewModel: VideoPlayerViewModel) {
    // 亮度调节提示
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

    // 音量调节提示
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
