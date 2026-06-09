package com.example.study.videoPlayer

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toDrawable
import android.net.Uri
import android.os.Build
import android.util.Size
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
            resolver.loadThumbnail(uri, Size(512, 288), null)
        } else null

        bitmap ?: throw IOException("loadThumbnail returned null for $uri")

        DrawableResult(
            drawable = bitmap.toDrawable(resources),
            isSampled = false,
            dataSource = DataSource.DISK,
        )
    }

    /** 通过构造函数传入 Context，避免依赖 ImageLoader 接口不暴露的 context 属性 */
    class Factory(context: Context) : Fetcher.Factory<Uri> {
        private val resolver = context.contentResolver
        private val resources = context.resources

        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? {
            if (data.scheme != "content") return null
            return VideoThumbnailFetcher(resolver, data, resources)
        }
    }
}
