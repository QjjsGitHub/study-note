package com.example.study.videoPlayer.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 视频列表 ViewModel — 管理扫描状态与视频列表数据。
 */
class VideoListViewModel(application: Application) : AndroidViewModel(application) {

    // ── UI 状态 ──────────────────────────────────────────────────────
    var videoList by mutableStateOf<List<VideoItem>>(emptyList())
        private set

    var isScanning by mutableStateOf(false)
        private set

    var hasScanned by mutableStateOf(false)
        private set

    // ── 一次性事件（Toast 等）────────────────────────────────────────
    private val _events = Channel<ScanEvent>(Channel.BUFFERED)
    val events: Flow<ScanEvent> = _events.receiveAsFlow()

    sealed class ScanEvent {
        data class Success(val count: Int) : ScanEvent()
        data class Error(val message: String) : ScanEvent()
    }

    // ── 内部依赖 ────────────────────────────────────────────────────
    private val repository = VideoRepository(application.contentResolver)

    // ── 公开方法 ────────────────────────────────────────────────────

    /** 扫描本地视频。调用前需确保已获得存储权限。 */
    fun scanVideos() {
        if (isScanning) return
        isScanning = true

        viewModelScope.launch {
            try {
                val videos = withContext(Dispatchers.IO) {
                    repository.scanAllVideos()
                }
                videoList = videos
                hasScanned = true
                _events.send(ScanEvent.Success(videos.size))
            } catch (e: Exception) {
                _events.send(ScanEvent.Error(e.message ?: "扫描失败"))
            } finally {
                isScanning = false
            }
        }
    }
}
