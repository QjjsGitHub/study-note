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
 * 处理视频扫描、排序、搜索逻辑。
 */
class VideoListViewModel(application: Application) : AndroidViewModel(application) {

    // ── UI 状态 ──────────────────────────────────────────────────────
    /** 原始视频列表（未过滤） */
    var videoList by mutableStateOf<List<VideoItem>>(emptyList())
        private set

    /** 所有视频文件的总大小（字节） */
    var totalSizeBytes by mutableStateOf(0L)
        private set

    /** 是否正在扫描中 */
    var isScanning by mutableStateOf(false)
        private set

    /** 是否已经执行过至少一次扫描 */
    var hasScanned by mutableStateOf(false)
        private set

    // ── 排序状态 ────────────────────────────────────────────────────

    /** 视频列表的排序模式 */
    enum class SortMode(val label: String) {
        NAME("按名称排序"),
        DATE("按日期排序"),
        SIZE("按大小排序"),
        DURATION("按时长排序")
    }

    /** 当前选中的排序模式 */
    var sortMode by mutableStateOf(SortMode.DATE)
        private set

    /** 排序后的视频列表 */
    private fun List<VideoItem>.sortedByMode(): List<VideoItem> {
        return when (sortMode) {
            SortMode.NAME -> sortedBy { it.title }
            SortMode.DATE -> sortedByDescending { it.dateModified }
            SortMode.SIZE -> sortedByDescending { it.fileSizeBytes }
            SortMode.DURATION -> sortedByDescending { it.durationMs }
        }
    }

    // ── 搜索状态 ────────────────────────────────────────────────────
    var isSearchActive by mutableStateOf(false)
        private set

    /** 用户实时输入的原始搜索文本（绑定到 TextField） */
    var searchQuery by mutableStateOf("")
        private set

    /** 经过防抖后实际用于过滤的搜索文本 */
    var debouncedQuery by mutableStateOf("")
        private set

    /** 按搜索内容过滤、按 sortMode 排序的视频列表（多关键词空格分隔，AND 逻辑） */
    val filteredVideoList: List<VideoItem>
        get() {
            val query = debouncedQuery.trim()
            val filtered = if (query.isEmpty()) {
                videoList
            } else {
                val keywords = query.split("\\s+".toRegex()).filter { it.isNotEmpty() }
                if (keywords.isEmpty()) videoList
                else videoList.filter { video ->
                    keywords.all { keyword ->
                        video.title.contains(keyword, ignoreCase = true) ||
                                video.filePath.contains(keyword, ignoreCase = true) ||
                                video.resolution.contains(keyword, ignoreCase = true)
                    }
                }
            }
            return filtered.sortedByMode()
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

    /** 激活搜索模式 */
    fun activateSearch() {
        isSearchActive = true
    }

    /** 退出搜索模式并重置查询关键字 */
    fun deactivateSearch() {
        isSearchActive = false
        searchQuery = ""
        debouncedQuery = ""
        searchDebounceJob?.cancel()
    }

    /** 
     * 更新搜索关键字。
     * 包含 200ms 的防抖处理，防止输入过快导致的频繁重组。
     */
    fun updateSearchQuery(query: String) {
        searchQuery = query
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(200L.milliseconds)
            debouncedQuery = query
        }
    }

    /** 更新排序模式 */
    fun updateSortMode(mode: SortMode) {
        sortMode = mode
    }

    /** 
     * 异步扫描本地视频。
     * 建议在调用前由 Activity/Fragment 确保已获得存储权限。
     */
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
