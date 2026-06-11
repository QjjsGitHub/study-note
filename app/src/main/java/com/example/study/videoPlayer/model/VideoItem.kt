package com.example.study.videoPlayer.model

/**
 * 本地视频数据模型
 */
data class VideoItem(
    val id: Long,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val width: Int = 0,
    val height: Int = 0,
    val resolution: String = "未知",
    /** MediaStore video content URI, used for thumbnail loading and player data source. */
    val contentUri: String? = null
) {
    /** 格式化时长，如 "01:23:45" 或 "12:34"（构造时预计算，避免滚动中重复算） */
    val formattedDuration: String = run {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

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

