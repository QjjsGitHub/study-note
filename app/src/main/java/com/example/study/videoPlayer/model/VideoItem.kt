package com.example.study.videoPlayer.model

import com.example.study.videoPlayer.util.formatTime

/**
 * 本地视频数据模型
 * 包含视频的元数据信息及预计算的 UI 展示文本。
 */
data class VideoItem(
    /** 数据库唯一 ID */
    val id: Long,
    /** 视频标题 */
    val title: String,
    /** 文件系统中的绝对路径 */
    val filePath: String,
    /** 视频时长（毫秒） */
    val durationMs: Long,
    /** 文件大小（字节） */
    val fileSizeBytes: Long,
    /** 上次修改时间戳（秒） */
    val dateModified: Long = 0L,
    /** 视频像素宽度 */
    val width: Int = 0,
    /** 视频像素高度 */
    val height: Int = 0,
    /** 分辨率文本，如 "1920×1080" */
    val resolution: String = "未知",
    /** MediaStore 视频 content URI，用于缩略图加载和播放器数据源 */
    val contentUri: String? = null
) {
    /** 格式化时长，如 "01:23:45" 或 "12:34"（构造时预计算，避免滚动中重复算） */
    val formattedDuration: String = formatTime(durationMs)

    /** 格式化文件大小（构造时预计算） */
    val formattedSize: String = run {
        val kb = fileSizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        when {
            gb >= 1.0 -> "%.1f GB".format(gb)
            mb >= 1.0 -> "%.1f MB".format(mb)
            else -> "%.0f KB".format(kb)
        }
    }

    /** 预计算副标题（分辨率 + 大小），减少 Composable 中的字符串拼接 */
    val displaySubtitle: String = "$resolution · $formattedSize"

    /** 简短标题：限制长度防止合并显示时挤占副标题行空间 */
    val shortTitle: String = if (title.length > 18) title.take(16) + "…" else title
}

