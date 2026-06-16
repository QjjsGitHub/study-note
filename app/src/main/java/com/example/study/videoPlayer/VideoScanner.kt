package com.example.study.videoPlayer

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.study.videoPlayer.model.VideoItem

/**
 * 本地视频扫描工具类
 * 利用 ContentResolver 查询 MediaStore 数据库，获取设备上的所有视频文件信息。
 */
object VideoScanner {

    /** 扫描视频时需要提取的数据库列字段 */
    private val VIDEO_PROJECTION = buildList {
        add(MediaStore.Video.Media._ID)
        add(MediaStore.Video.Media.TITLE)
        add(MediaStore.Video.Media.DATA)
        add(MediaStore.Video.Media.DURATION)
        add(MediaStore.Video.Media.SIZE)
        add(MediaStore.Video.Media.DATE_MODIFIED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(MediaStore.Video.Media.WIDTH)
            add(MediaStore.Video.Media.HEIGHT)
            add(MediaStore.Video.Media.ORIENTATION)
        } else {
            // Android 9 以下 MediaStore.Video.Media.WIDTH/HEIGHT 常量不可用，
            // 但底层的 sqlite 列名 "width"/"height" 自 Android 1.0 起一直存在且稳定，
            // 直接使用字符串是安全的。
            add("width")
            add("height")
        }
    }.toTypedArray()

    /** 默认排序规则：按修改日期降序排列（最新的在前面） */
    private const val SORT_ORDER = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

    /**
     * 执行全盘视频扫描
     * @param contentResolver 系统内容解析器
     * @return 包含所有发现的视频信息的列表
     */
    fun scanAllVideos(contentResolver: ContentResolver): List<VideoItem> {
        val videos = mutableListOf<VideoItem>()
        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            VIDEO_PROJECTION,
            null,
            null,
            SORT_ORDER
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val widthCol = cursor.getColumnIndex("width")
            val heightCol = cursor.getColumnIndex("height")
            val orientCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Video.Media.ORIENTATION)
            } else -1

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                var width = if (widthCol >= 0) cursor.getInt(widthCol) else 0
                var height = if (heightCol >= 0) cursor.getInt(heightCol) else 0
                val orientation = if (orientCol >= 0) cursor.getInt(orientCol) else 0

                // 如果视频带有 90 或 270 度的旋转元数据，需要交换宽高值以符合实际显示的视觉宽高
                if (orientation == 90 || orientation == 270) {
                    val temp = width
                    width = height
                    height = temp
                }

                videos.add(
                    VideoItem(
                        id = id,
                        title = cursor.getString(titleCol) ?: "未知视频",
                        filePath = cursor.getString(dataCol) ?: continue,
                        durationMs = cursor.getLong(durationCol),
                        fileSizeBytes = cursor.getLong(sizeCol),
                        dateModified = cursor.getLong(dateCol),
                        width = width,
                        height = height,
                        resolution = if (width > 0 && height > 0) "${width}×${height}" else "未知",
                        contentUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString()).toString()
                    )
                )
            }
        }
        return videos
    }
}
