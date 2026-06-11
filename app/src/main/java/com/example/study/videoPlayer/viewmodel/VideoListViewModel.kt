package com.example.study.videoPlayer.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * 视频列表 ViewModel — 管理扫描状态与视频列表数据。
 */
class VideoListViewModel(application: Application) : AndroidViewModel(application) {

    // ── UI 状态 ──────────────────────────────────────────────────────
    var videoList by mutableStateOf<List<VideoItem>>(emptyList())
        private set

    var totalSizeBytes by mutableStateOf(0L)
        private set

    var isScanning by mutableStateOf(false)
        private set

    var hasScanned by mutableStateOf(false)
        private set

    // ── 搜索状态 ────────────────────────────────────────────────────
    var isSearchActive by mutableStateOf(false)
        private set

    /** 用户实时输入的原始搜索文本（绑定到 TextField） */
    var searchQuery by mutableStateOf("")
        private set

    /** 经过防抖后实际用于过滤的搜索文本 */
    var debouncedQuery by mutableStateOf("")
        private set

    /** 按搜索内容过滤的视频列表（多关键词空格分隔，AND 逻辑） */
    val filteredVideoList: List<VideoItem>
        get() {
            val query = debouncedQuery.trim()
            if (query.isEmpty()) return videoList
            val keywords = query.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            if (keywords.isEmpty()) return videoList
            return videoList.filter { video ->
                keywords.all { keyword ->
                    video.title.contains(keyword, ignoreCase = true) ||
                            video.filePath.contains(keyword, ignoreCase = true) ||
                            video.resolution.contains(keyword, ignoreCase = true)
                }
            }
        }

    // ── 防抖 Job
    private var searchDebounceJob: Job? = null

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

    // ── 搜索控制 ──────────────────────────────────────────────────

    fun activateSearch() {
        isSearchActive = true
    }

    fun deactivateSearch() {
        isSearchActive = false
        searchQuery = ""
        debouncedQuery = ""
        searchDebounceJob?.cancel()
    }

    /** 输入时调用，200ms 防抖后更新过滤关键字 */
    fun updateSearchQuery(query: String) {
        searchQuery = query
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(200L.milliseconds)
            debouncedQuery = query
        }
    }

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
                totalSizeBytes = videos.sumOf { it.fileSizeBytes }
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
