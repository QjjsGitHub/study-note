package com.example.study.videoPlayer

import android.content.ContentResolver
import android.media.MediaMetadataRetriever
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
     * 通过 MediaMetadataRetriever 获取旋转后的真实显示宽高。
     * MediaStore 的 WIDTH/HEIGHT 不包含旋转元数据。
     */
    private fun getDisplaySize(
        filePath: String,
        rawWidth: Int,
        rawHeight: Int,
    ): Pair<Int, Int> {
        if (rawWidth <= 0 || rawHeight <= 0) return 0 to 0
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val rotation = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
            )?.toIntOrNull() ?: 0
            retriever.release()
            if (rotation == 90 || rotation == 270) rawHeight to rawWidth
            else rawWidth to rawHeight
        } catch (_: Exception) {
            rawWidth to rawHeight
        }
    }

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
                val rawWidth = if (widthColumn >= 0) it.getInt(widthColumn) else 0
                val rawHeight = if (heightColumn >= 0) it.getInt(heightColumn) else 0

                // 构建视频内容 URI（用于缩略图等）
                val contentUri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                // MediaStore 的 WIDTH/HEIGHT 不含旋转信息，用 MediaMetadataRetriever 修正
                val (displayWidth, displayHeight) = getDisplaySize(filePath, rawWidth, rawHeight)
                val resolution = if (displayWidth > 0 && displayHeight > 0) "${displayWidth}×${displayHeight}" else "未知"

                videos.add(
                    VideoItem(
                        id = id,
                        title = title,
                        filePath = filePath,
                        durationMs = durationMs,
                        fileSizeBytes = fileSizeBytes,
                        width = displayWidth,
                        height = displayHeight,
                        resolution = resolution,
                        thumbnailPath = contentUri.toString()
                    )
                )
            }
        }

        return videos
    }
}