package com.example.study.videoPlayer.util

/**
 * 时间格式化工具类
 */

/**
 * 将毫秒数格式化为可读的时间字符串。
 * 如果时长超过一小时，显示格式为 "H:MM:SS"；
 * 如果时长不足一小时，显示格式为 "MM:SS"。
 * 
 * @param ms 毫秒数
 * @return 格式化后的时间字符串
 */
fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
