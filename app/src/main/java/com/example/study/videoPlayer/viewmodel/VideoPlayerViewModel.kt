package com.example.study.videoPlayer.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.util.Log
import android.view.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.videoPlayer.model.VideoItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 视频播放 ViewModel — 管理 MediaPlayer 生命周期、播放状态、亮度/音量。
 */
class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {

    // ════════════════════════════════════════════════════════════════
    // 播放状态
    // ════════════════════════════════════════════════════════════════
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

    // ════════════════════════════════════════════════════════════════
    // UI 控制状态
    // ════════════════════════════════════════════════════════════════
    var showControls by mutableStateOf(true)
        private set
    var showSpeedMenu by mutableStateOf(false)
    var showBrightnessOverlay by mutableStateOf(false)
    var showVolumeOverlay by mutableStateOf(false)

    // ════════════════════════════════════════════════════════════════
    // 亮度 / 音量（由 composable 同步到系统）
    // ════════════════════════════════════════════════════════════════
    var brightness by mutableFloatStateOf(0.5f)
        private set
    var volume by mutableFloatStateOf(0.5f)
        private set

    // ════════════════════════════════════════════════════════════════
    // 内部
    // ════════════════════════════════════════════════════════════════
    private val mediaPlayer = MediaPlayer()
    private var currentVideo: VideoItem? = null
    private var currentSurface: Surface? = null
    private var progressJob: Job? = null
    private var controlsAutoHideJob: Job? = null

    init {
        mediaPlayer.setOnPreparedListener { mp ->
            isPrepared = true
            mp.start()
            isPlaying = true
            if (playbackSpeed != 1.0f) {
                mp.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
            }
            startProgressPolling()
            scheduleControlsAutoHide()
        }

        mediaPlayer.setOnErrorListener { _, _, _ ->
            hasError = true
            isPrepared = false
            stopProgressPolling()
            true
        }

        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            currentVideo?.let {
                currentPositionMs = it.durationMs.toFloat()
            }
            stopProgressPolling()
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Surface 管理
    // ════════════════════════════════════════════════════════════════

    /**
     * TextureView Surface 就绪时调用。
     */
    fun onSurfaceReady(surface: Surface, video: VideoItem) {
        currentSurface = surface
        currentVideo = video
        surfaceReady = true
        preparePlayer(video, surface)
    }

    /**
     * TextureView Surface 销毁时调用。
     */
    fun onSurfaceDestroyed() {
        stopProgressPolling()
        try {
            mediaPlayer.pause()
        } catch (_: Exception) {}
        isPlaying = false
        surfaceReady = false
        currentSurface?.release()
        currentSurface = null
    }

    // ════════════════════════════════════════════════════════════════
    // 播放控制
    // ════════════════════════════════════════════════════════════════

    fun togglePlayPause() {
        if (!isPrepared) return
        try {
            if (isPlaying) {
                mediaPlayer.pause()
                stopProgressPolling()
            } else {
                mediaPlayer.start()
                startProgressPolling()
                scheduleControlsAutoHide()
            }
            isPlaying = !isPlaying
        } catch (_: Exception) {}
    }

    fun seekTo(seekMs: Int) {
        if (isPrepared) {
            try {
                mediaPlayer.seekTo(seekMs)
            } catch (_: Exception) {}
        }
        currentPositionMs = seekMs.toFloat()
    }

    fun skipBackward() {
        if (!isPrepared) return
        val newPos = (mediaPlayer.currentPosition - 10_000).coerceAtLeast(0)
        mediaPlayer.seekTo(newPos)
        currentPositionMs = newPos.toFloat()
    }

    fun skipForward() {
        if (!isPrepared) return
        val limit = currentVideo?.durationMs?.toInt() ?: return
        val newPos = (mediaPlayer.currentPosition + 10_000).coerceAtMost(limit)
        mediaPlayer.seekTo(newPos)
        currentPositionMs = newPos.toFloat()
    }

    fun setSpeed(speed: Float) {
        playbackSpeed = speed
        if (isPrepared) {
            try {
                mediaPlayer.playbackParams = PlaybackParams().setSpeed(speed)
            } catch (_: Exception) {}
        }
    }

    // ════════════════════════════════════════════════════════════════
    // UI 控制
    // ════════════════════════════════════════════════════════════════

    fun toggleControls() { showControls = !showControls }
    fun showControls() {
        showControls = true
        scheduleControlsAutoHide()
    }

    fun dismissSpeedMenu() { showSpeedMenu = false }

    fun showBrightnessIndicator() { showBrightnessOverlay = true }
    fun showVolumeIndicator() { showVolumeOverlay = true }

    /** 更新亮度值（范围 0.01~1f） */
    fun updateBrightness(value: Float) {
        brightness = value.coerceIn(0.01f, 1f)
    }

    /** 更新音量值（范围 0~1f） */
    fun updateVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
    }

    // ════════════════════════════════════════════════════════════════
    // 生命周期
    // ════════════════════════════════════════════════════════════════

    /** Activity onPause 时调用 */
    fun onPause() {
        if (isPlaying) {
            try {
                mediaPlayer.pause()
            } catch (_: Exception) {}
            isPlaying = false
            stopProgressPolling()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressPolling()
        cancelControlsAutoHide()
        try {
            mediaPlayer.release()
        } catch (_: Exception) {}
        try {
            currentSurface?.release()
            currentSurface = null
        } catch (_: Exception) {}
    }

    // ════════════════════════════════════════════════════════════════
    // 私有方法
    // ════════════════════════════════════════════════════════════════

    private fun preparePlayer(video: VideoItem, surface: Surface) {
        try {
            mediaPlayer.reset()
            isPrepared = false
            hasError = false
            stopProgressPolling()

            val contentUri = video.thumbnailPath?.let { Uri.parse(it) }
            if (contentUri != null && contentUri.scheme == "content") {
                mediaPlayer.setDataSource(getApplication(), contentUri)
            } else {
                mediaPlayer.setDataSource(video.filePath)
            }

            mediaPlayer.setSurface(surface)
            mediaPlayer.prepareAsync()
        } catch (_: Exception) {
            hasError = true
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isPlaying && isPrepared) {
                try {
                    currentPositionMs = mediaPlayer.currentPosition.toFloat()
                } catch (_: Exception) {}
                delay(1000L)
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
            delay(4000L)
            if (isPlaying) {
                showControls = false
            }
        }
    }

    private fun cancelControlsAutoHide() {
        controlsAutoHideJob?.cancel()
        controlsAutoHideJob = null
    }
}
