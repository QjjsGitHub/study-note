package com.example.study.videoPlayer.repository

import android.content.ContentResolver
import com.example.study.videoPlayer.VideoScanner
import com.example.study.videoPlayer.model.VideoItem

/**
 * 视频数据仓库 — 封装 [VideoScanner]，作为 ViewModel 与数据源之间的抽象层。
 */
class VideoRepository(private val contentResolver: ContentResolver) {

    /**
     * 扫描设备上的所有本地视频（IO 操作，需在协程中调用）。
     */
    suspend fun scanAllVideos(): List<VideoItem> {
        return VideoScanner.scanAllVideos(contentResolver)
    }
}
