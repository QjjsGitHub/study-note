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
    val resolution: String,
    val thumbnailPath: String? = null
) {
    /** 格式化时长，如 "01:23:45" 或 "12:34" */
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                "%02d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%02d:%02d".format(minutes, seconds)
            }
        }

    /** 格式化文件大小 */
    val formattedSize: String
        get() {
            val kb = fileSizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> "%.1f GB".format(gb)
                mb >= 1.0 -> "%.1f MB".format(mb)
                else -> "%.0f KB".format(kb)
            }
        }
}

/**
 * 模拟本地视频列表数据
 */
object MockVideos {
    fun getVideos(): List<VideoItem> = listOf(
        VideoItem(
            id = 1,
            title = "假期旅行记录.mp4",
            filePath = "/storage/emulated/0/Movies/假期旅行记录.mp4",
            durationMs = 5_430_000,  // 1:30:30
            fileSizeBytes = 1_280_000_000,
            resolution = "1920×1080"
        ),
        VideoItem(
            id = 2,
            title = "生日聚会.mp4",
            filePath = "/storage/emulated/0/Movies/生日聚会.mp4",
            durationMs = 2_145_000,  // 35:45
            fileSizeBytes = 456_000_000,
            resolution = "1280×720"
        ),
        VideoItem(
            id = 3,
            title = "教程视频 - Kotlin入门.mp4",
            filePath = "/storage/emulated/0/Download/教程视频 - Kotlin入门.mp4",
            durationMs = 7_200_000,  // 2:00:00
            fileSizeBytes = 2_560_000_000,
            resolution = "1920×1080"
        ),
        VideoItem(
            id = 4,
            title = "无人机航拍风景.mp4",
            filePath = "/storage/emulated/0/DCIM/无人机航拍风景.mp4",
            durationMs = 482_000,  // 8:02
            fileSizeBytes = 320_000_000,
            resolution = "3840×2160"
        ),
        VideoItem(
            id = 5,
            title = "会议录制 2024-03-15.mp4",
            filePath = "/storage/emulated/0/Recordings/会议录制 2024-03-15.mp4",
            durationMs = 3_660_000,  // 1:01:00
            fileSizeBytes = 890_000_000,
            resolution = "1920×1080"
        ),
        VideoItem(
            id = 6,
            title = "动画短片.mp4",
            filePath = "/storage/emulated/0/Movies/动画短片.mp4",
            durationMs = 187_000,  // 3:07
            fileSizeBytes = 45_000_000,
            resolution = "1280×720"
        ),
        VideoItem(
            id = 7,
            title = "健身教程 - 全身燃脂.mp4",
            filePath = "/storage/emulated/0/Download/健身教程 - 全身燃脂.mp4",
            durationMs = 1_800_000,  // 30:00
            fileSizeBytes = 520_000_000,
            resolution = "1920×1080"
        ),
        VideoItem(
            id = 8,
            title = "音乐会现场.mp4",
            filePath = "/storage/emulated/0/Music/音乐会现场.mp4",
            durationMs = 10_800_000,  // 3:00:00
            fileSizeBytes = 4_200_000_000,
            resolution = "3840×2160"
        ),
        VideoItem(
            id = 9,
            title = "Vlog 日常.mp4",
            filePath = "/storage/emulated/0/Movies/Vlog 日常.mp4",
            durationMs = 920_000,  // 15:20
            fileSizeBytes = 280_000_000,
            resolution = "1920×1080"
        ),
        VideoItem(
            id = 10,
            title = "网课 - 数据结构第3讲.mp4",
            filePath = "/storage/emulated/0/Download/网课 - 数据结构第3讲.mp4",
            durationMs = 5_040_000,  // 1:24:00
            fileSizeBytes = 1_050_000_000,
            resolution = "1280×720"
        )
    )
}