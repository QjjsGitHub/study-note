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

    private val VIDEO_PROJECTION = buildList {
        add(MediaStore.Video.Media._ID)
        add(MediaStore.Video.Media.TITLE)
        add(MediaStore.Video.Media.DATA)
        add(MediaStore.Video.Media.DURATION)
        add(MediaStore.Video.Media.SIZE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(MediaStore.Video.Media.WIDTH)
            add(MediaStore.Video.Media.HEIGHT)
            add(MediaStore.Video.Media.ORIENTATION)
        } else {
            add("width")
            add("height")
        }
    }.toTypedArray()

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
            val widthColumn = it.getColumnIndex("width")
            val heightColumn = it.getColumnIndex("height")
            val orientationColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.getColumnIndex(MediaStore.Video.Media.ORIENTATION)
            } else -1

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn) ?: "未知视频"
                val filePath = it.getString(dataColumn) ?: continue
                val durationMs = it.getLong(durationColumn)
                val fileSizeBytes = it.getLong(sizeColumn)
                var width = if (widthColumn >= 0) it.getInt(widthColumn) else 0
                var height = if (heightColumn >= 0) it.getInt(heightColumn) else 0
                val orientation = if (orientationColumn >= 0) it.getInt(orientationColumn) else 0

                // 如果有旋转信息，交换宽高
                if (orientation == 90 || orientation == 270) {
                    val temp = width
                    width = height
                    height = temp
                }

                // 构建视频内容 URI
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                val resolution = if (width > 0 && height > 0) "${width}×${height}" else "未知"

                videos.add(
                    VideoItem(
                        id = id,
                        title = title,
                        filePath = filePath,
                        durationMs = durationMs,
                        fileSizeBytes = fileSizeBytes,
                        width = width,
                        height = height,
                        resolution = resolution,
                        contentUri = contentUri.toString()
                    )
                )
            }
        }

        return videos
    }
}
