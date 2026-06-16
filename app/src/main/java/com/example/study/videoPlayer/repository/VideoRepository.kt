package com.example.study.videoPlayer.repository

import android.content.ContentResolver
import com.example.study.videoPlayer.VideoScanner
import com.example.study.videoPlayer.model.VideoItem

/**
 * 视频数据仓库
 * 封装 [VideoScanner]，作为 ViewModel 与数据源（MediaStore）之间的抽象层。
 * 方便未来扩展数据源（如数据库缓存或网络数据）。
 */
class VideoRepository(private val contentResolver: ContentResolver) {

    /**
     * 扫描设备上的所有本地视频。
     * 这是一个挂起函数，应在 IO 调度器中执行。
     * 
     * @return 视频条目列表
     */
    suspend fun scanAllVideos(): List<VideoItem> {
        return VideoScanner.scanAllVideos(contentResolver)
    }
}
