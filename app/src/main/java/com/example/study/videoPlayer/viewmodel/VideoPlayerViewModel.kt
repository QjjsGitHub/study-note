package com.example.study.videoPlayer.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import android.view.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.videoPlayer.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds

/**
 * 视频播放 ViewModel — 管理 MediaPlayer 生命周期、播放状态、亮度/音量。
 */
class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {

    // ── 播放状态 ───────────────────────────────────────────────────
    var isPlaying by mutableStateOf(false)
        private set
    var isPrepared by mutableStateOf(false)
        private set
    var hasError by mutableStateOf(false)
        private set
    var currentPositionMs by mutableFloatStateOf(0f)
        private set
    var playbackSpeed by mutableFloatStateOf(1.0f)
        private set

    /** Surface 是否已就绪 */
    var surfaceReady by mutableStateOf(false)
        private set

    // ── UI 控制状态 ────────────────────────────────────────────────
    var showControls by mutableStateOf(true)
        private set
    var showSpeedMenu by mutableStateOf(false)
    var showBrightnessOverlay by mutableStateOf(false)
        private set
    var showVolumeOverlay by mutableStateOf(false)
        private set

    // ── 亮度 / 音量（由 composable 同步到系统） ──────────────────────
    var brightness by mutableFloatStateOf(0.5f)
        private set
    var volume by mutableFloatStateOf(0.5f)
        private set

    // ── 内部资源 ───────────────────────────────────────────────────
    private val mediaPlayer = MediaPlayer()
    private var currentVideo: VideoItem? = null
    private var currentSurface: Surface? = null
    private var progressJob: Job? = null
    private var prepareJob: Job? = null
    private var controlsAutoHideJob: Job? = null

    init {
        setupMediaPlayer()
    }

    private fun setupMediaPlayer() {
        mediaPlayer.setOnPreparedListener { mp ->
            isPrepared = true
            hasError = false
            mp.start()
            isPlaying = true
            updatePlaybackSpeed(playbackSpeed)
            startProgressPolling()
            scheduleControlsAutoHide()
        }

        mediaPlayer.setOnErrorListener { _, _, _ ->
            hasError = true
            isPrepared = false
            stopProgressPolling()
            false // 不拦截，让系统可能弹出对话框或记录日志
        }

        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            currentVideo?.let {
                currentPositionMs = it.durationMs.toFloat()
            }
            stopProgressPolling()
            showControls = true
        }
    }

    // ── Surface 管理 ────────────────────────────────────────────────

    fun onSurfaceReady(surface: Surface, video: VideoItem) {
        val isSamePreparedVideo = currentVideo?.id == video.id && isPrepared
        currentSurface = surface
        currentVideo = video
        surfaceReady = true

        if (isSamePreparedVideo) {
            safeExecute {
                mediaPlayer.setSurface(surface)
            }
        } else {
            preparePlayer(video, surface)
        }
    }

    fun onSurfaceDestroyed() {
        stopProgressPolling()
        safeExecute {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
            mediaPlayer.setSurface(null)
        }
        isPlaying = false
        surfaceReady = false
        currentSurface?.release()
        currentSurface = null
    }

    // ── 播放控制 ───────────────────────────────────────────────────

    fun togglePlayPause() {
        if (!isPrepared) return
        safeExecute {
            if (isPlaying) {
                isPlaying = false
                mediaPlayer.pause()
                stopProgressPolling()
            } else {
                mediaPlayer.start()
                isPlaying = true
                startProgressPolling()
                scheduleControlsAutoHide()
            }
        }
    }

    fun seekTo(seekMs: Int) {
        if (isPrepared) {
            safeExecute {
                mediaPlayer.seekTo(seekMs)
                currentPositionMs = seekMs.toFloat()
            }
        }
    }

    fun skipBackward() {
        if (!isPrepared) return
        val newPos = (mediaPlayer.currentPosition - 10_000).coerceAtLeast(0)
        seekTo(newPos)
    }

    fun skipForward() {
        if (!isPrepared) return
        val limit = currentVideo?.durationMs?.toInt() ?: return
        val newPos = (mediaPlayer.currentPosition + 10_000).coerceAtMost(limit)
        seekTo(newPos)
    }

    fun setSpeed(speed: Float) {
        playbackSpeed = speed
        updatePlaybackSpeed(speed)
    }

    private fun updatePlaybackSpeed(speed: Float) {
        if (isPrepared) {
            safeExecute {
                mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(speed)
                // 某些 Android 版本在设置 playbackParams 时会隐式调用 start()，
                // 如果视频原本是暂停状态，需要重新暂停以保持状态一致。
                if (!isPlaying) {
                    mediaPlayer.pause()
                }
            }
        }
    }

    // ── UI 控制 ────────────────────────────────────────────────────

    fun toggleControls() {
        showControls = !showControls
        if (showControls) {
            refreshPosition()
            scheduleControlsAutoHide()
        }
    }

    fun showControls() {
        showControls = true
        refreshPosition()
        scheduleControlsAutoHide()
    }

    fun dismissSpeedMenu() {
        showSpeedMenu = false
    }

    fun showBrightnessAdjusting() {
        showBrightnessOverlay = true
    }

    fun dismissBrightnessOverlay() {
        showBrightnessOverlay = false
    }

    fun showVolumeAdjusting() {
        showVolumeOverlay = true
    }

    fun dismissVolumeOverlay() {
        showVolumeOverlay = false
    }

    fun updateBrightness(value: Float) {
        brightness = value.coerceIn(0.01f, 1f)
    }

    fun updateVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
    }

    // ── 生命周期 ───────────────────────────────────────────────────

    fun onPause() {
        if (isPlaying) {
            safeExecute { mediaPlayer.pause() }
            isPlaying = false
            stopProgressPolling()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressPolling()
        cancelControlsAutoHide()
        safeExecute { mediaPlayer.release() }
        currentSurface?.release()
    }

    // ── 私有助手 ────────────────────────────────────────────────────

    private fun preparePlayer(video: VideoItem, surface: Surface) {
        // 取消上一个未完成的准备任务，避免并发操作 MediaPlayer
        prepareJob?.cancel()
        isPrepared = false
        hasError = false
        stopProgressPolling()

        prepareJob = viewModelScope.launch {
            try {
                // 将重量级同步操作（reset + setDataSource）移到 IO 线程
                // 避免阻塞主线程导致首次点击卡顿
                withContext(Dispatchers.IO) {
                    mediaPlayer.reset()
                    val uri = video.contentUri?.toUri()
                    if (uri?.scheme == "content") {
                        mediaPlayer.setDataSource(getApplication(), uri)
                    } else {
                        mediaPlayer.setDataSource(video.filePath)
                    }
                }
                // 回到主线程设置 Surface 并异步准备播放
                mediaPlayer.setSurface(surface)
                mediaPlayer.prepareAsync()
            } catch (e: Exception) {
                Log.e("VideoPlayerVM", "preparePlayer Error", e)
                hasError = true
                isPrepared = false
            }
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive && isPlaying && isPrepared) {
                safeExecute {
                    currentPositionMs = mediaPlayer.currentPosition.toFloat()
                }
                delay(if (showControls) 250L.milliseconds else 1000L.milliseconds)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun scheduleControlsAutoHide() {
        cancelControlsAutoHide()
        controlsAutoHideJob = viewModelScope.launch {
            delay(5000L.milliseconds)
            if (isPlaying && isActive) {
                showControls = false
            }
        }
    }

    private fun cancelControlsAutoHide() {
        controlsAutoHideJob?.cancel()
        controlsAutoHideJob = null
    }

    /** 安全执行 MediaPlayer 操作，防止 IllegalStateException 导致崩溃 */
    private inline fun safeExecute(action: () -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            Log.e("VideoPlayerVM", "MediaPlayer Error", e)
        }
    }

    /** 立即从 MediaPlayer 读取最新播放进度 */
    private fun refreshPosition() {
        if (isPrepared) {
            safeExecute {
                currentPositionMs = mediaPlayer.currentPosition.toFloat()
            }
        }
    }
}
