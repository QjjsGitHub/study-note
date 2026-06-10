package com.example.study.videoPlayer

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.Fetcher
import coil.fetch.FetchResult
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Coil Fetcher：将 video content URI 转成 [ContentResolver.loadThumbnail] 缩略图。
 */
class VideoThumbnailFetcher(
    private val resolver: ContentResolver,
    private val uri: Uri,
    private val resources: android.content.res.Resources,
) : Fetcher {

    override suspend fun fetch(): FetchResult = withContext(Dispatchers.IO) {
        val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                resolver.loadThumbnail(uri, Size(512, 288), null)
            } catch (_: Exception) {
                null
            }
        } else {
            // Android Q 以下的兼容处理
            @Suppress("DEPRECATION")
            android.media.ThumbnailUtils.createVideoThumbnail(
                getPathFromUri(uri) ?: "",
                MediaStore.Video.Thumbnails.MINI_KIND
            )
        }

        bitmap ?: throw IOException("Could not load thumbnail for $uri")

        DrawableResult(
            drawable = bitmap.toDrawable(resources),
            isSampled = false,
            dataSource = DataSource.DISK,
        )
    }

    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf<String>(MediaStore.Video.Media.DATA)
        return try {
            resolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /** 通过构造函数传入 Context，并存为私有属性，以便在 create 中使用 */
    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? {
            if (data.scheme != "content") return null
            return VideoThumbnailFetcher(context.contentResolver, data, context.resources)
        }
    }
}
