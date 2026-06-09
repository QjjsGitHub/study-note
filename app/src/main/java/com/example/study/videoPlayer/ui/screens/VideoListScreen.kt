package com.example.study.videoPlayer.ui.screens

import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.ui.theme.VideoControlBg
import com.example.study.videoPlayer.ui.theme.VideoOnSurfaceVariant
import com.example.study.videoPlayer.ui.theme.VideoPrimary
import com.example.study.videoPlayer.ui.theme.VideoSurfaceVariant

// 预创建 shape / 颜色，避免组合中反复 new
private val CardShape = RoundedCornerShape(12.dp)
private val BadgeShape = RoundedCornerShape(4.dp)
private val CardBg = VideoSurfaceVariant.copy(alpha = 0.4f)
private val HeaderBg = VideoSurfaceVariant.copy(alpha = 0.5f)
private val BadgeBg = VideoPrimary.copy(alpha = 0.15f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    videos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onRefresh: () -> Unit = {},
    onScan: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = VideoPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "本地视频",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 搜索 */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("刷新列表") },
                                onClick = { showMenu = false; onRefresh() }
                            )
                            DropdownMenuItem(
                                text = { Text("排序方式") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("扫描目录") },
                                onClick = { showMenu = false; onScan() }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = VideoOnSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无本地视频",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VideoOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右上角「扫描目录」查找视频文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = VideoOnSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 头部统计信息
                item(span = { GridItemSpan(2) }) { StorageInfoHeader(videos) }
                // 视频列表
                items(videos, key = { it.id }) { video ->
                    VideoCard(video = video, onClick = { onVideoClick(video) })
                }
                // 底部间距
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun StorageInfoHeader(videos: List<VideoItem>) {
    val totalCount = videos.size
    val totalGb = remember(videos) {
        videos.sumOf { it.fileSizeBytes } / (1024.0 * 1024.0 * 1024.0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderBg, shape = CardShape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "共 $totalCount 个视频",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "总大小 %.1f GB".format(totalGb),
                style = MaterialTheme.typography.bodySmall,
                color = VideoOnSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = "本地存储",
            style = MaterialTheme.typography.labelSmall,
            color = VideoPrimary,
            modifier = Modifier
                .background(BadgeBg, shape = BadgeShape)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─── Item Card ────────────────────────────────────────────────────────────────

@Composable
private fun VideoCard(video: VideoItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg, shape = CardShape)
            .clickable(onClick = onClick)
    ) {
        // 缩略图 — aspectRatio 从宽度推导高度，无需文本测量
        ThumbnailPlaceholder(video)

        // 固定高度文本区 — 避免每次滚动进入视野时测量文字

        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp,
            modifier = Modifier
                .padding(start = 8.dp, top = 6.dp)
                .height(24.dp)
        )
        Text(
            text = video.resolution + " · " + video.formattedSize,
            style = MaterialTheme.typography.labelSmall,
            color = VideoOnSurfaceVariant,
            maxLines = 1,
            modifier = Modifier
                .padding(start = 10.dp)
                .height(16.dp)
        )

    }
}

// ─── Thumbnail ────────────────────────────────────────────────────────────────

@Composable
private fun ThumbnailPlaceholder(video: VideoItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(VideoSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Coil AsyncImage — 自动缓存、取消、线程管理
        AsyncImage(
            model = video.thumbnailPath,
            contentDescription = video.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 时长标签（右下角）
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .background(VideoControlBg, shape = BadgeShape)
                .padding(horizontal = 5.dp, vertical = 0.dp)
        ) {
            Text(
                text = video.formattedDuration,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}
