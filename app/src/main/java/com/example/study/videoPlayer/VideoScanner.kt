package com.example.study.videoPlayer

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.study.videoPlayer.model.VideoItem

/**
 * 扫描本地视频文件
 */
object VideoScanner {

    private val VIDEO_PROJECTION = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.WIDTH
        } else {
            "width"
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.HEIGHT
        } else {
            "height"
        }
    )

    private const val SORT_ORDER = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

    /**
     * 扫描设备上的所有本地视频
     */
    fun scanAllVideos(contentResolver: ContentResolver): List<VideoItem> {
        val videos = mutableListOf<VideoItem>()
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val cursor = contentResolver.query(
            uri,
            VIDEO_PROJECTION,
            null,
            null,
            SORT_ORDER
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val widthColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            } else {
                it.getColumnIndex("width")
            }
            val heightColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            } else {
                it.getColumnIndex("height")
            }

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn) ?: "未知视频"
                val filePath = it.getString(dataColumn) ?: continue
                val durationMs = it.getLong(durationColumn)
                val fileSizeBytes = it.getLong(sizeColumn)
                val width = if (widthColumn >= 0) it.getInt(widthColumn) else 0
                val height = if (heightColumn >= 0) it.getInt(heightColumn) else 0
                val resolution = if (width > 0 && height > 0) "${width}×${height}" else "未知"

                // 构建视频内容 URI（用于缩略图等）
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                videos.add(
                    VideoItem(
                        id = id,
                        title = title,
                        filePath = filePath,
                        durationMs = durationMs,
                        fileSizeBytes = fileSizeBytes,
                        resolution = resolution,
                        thumbnailPath = contentUri.toString()
                    )
                )
            }
        }

        return videos
    }
}