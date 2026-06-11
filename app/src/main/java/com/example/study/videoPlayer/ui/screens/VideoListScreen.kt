package com.example.study.videoPlayer.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.ui.theme.VideoControlBg
import com.example.study.videoPlayer.ui.theme.VideoOnSurfaceVariant
import com.example.study.videoPlayer.ui.theme.VideoPrimary
import com.example.study.videoPlayer.ui.theme.VideoSurfaceVariant
import com.example.study.videoPlayer.viewmodel.VideoListViewModel

// 预创建 shape / 颜色，避免组合中反复 new
private val CardShape = RoundedCornerShape(12.dp)
private val BadgeShape = RoundedCornerShape(4.dp)
private val CardBg = VideoSurfaceVariant.copy(alpha = 0.4f)
private val HeaderBg = VideoSurfaceVariant.copy(alpha = 0.5f)
private val BadgeBg = VideoPrimary.copy(alpha = 0.15f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    viewModel: VideoListViewModel,
    onVideoClick: (VideoItem) -> Unit,
    onRefresh: () -> Unit = {},
    onScan: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // 进入搜索模式时自动聚焦输入框
    LaunchedEffect(viewModel.isSearchActive) {
        if (viewModel.isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (viewModel.isSearchActive) {
                // ── 搜索栏 ──
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            viewModel.deactivateSearch()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "关闭搜索",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    title = {
                        TextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = {
                                Text(
                                    text = "搜索标题、路径、分辨率…",
                                    color = VideoOnSurfaceVariant
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                cursorColor = VideoPrimary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { focusManager.clearFocus() }
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    },
                    actions = {
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "清除",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            } else {
                // ── 普通顶栏 ──
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
                        IconButton(onClick = { viewModel.activateSearch() }) {
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
        }
    ) { padding ->
        // 使用过滤后的列表进行展示
        val displayList = viewModel.filteredVideoList
        val hasQuery = viewModel.debouncedQuery.isNotEmpty()

        if (viewModel.isScanning) {
            // 扫描中 — 显示加载进度圈
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = VideoPrimary,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "正在扫描本地视频…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VideoOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "正在搜索 .mp4 .mkv .avi .mov 等格式",
                        style = MaterialTheme.typography.bodySmall,
                        color = VideoOnSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else if (hasQuery && displayList.isEmpty()) {
            // 搜索无结果
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = VideoOnSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "未找到匹配的视频",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VideoOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "试试其他关键词",
                        style = MaterialTheme.typography.bodySmall,
                        color = VideoOnSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else if (displayList.isEmpty() && !viewModel.hasScanned) {
            // 未扫描过且无数据
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
                // 头部统计信息（仅在非搜索模式下显示）
                if (!hasQuery) {
                    item(span = { GridItemSpan(2) }) {
                        StorageInfoHeader(
                            totalCount = viewModel.videoList.size,
                            totalSizeBytes = viewModel.totalSizeBytes
                        )
                    }
                } else {
                    // 搜索模式：显示过滤结果数
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HeaderBg, shape = CardShape)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "找到 ${displayList.size} 个匹配视频",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "\"${viewModel.debouncedQuery}\"",
                                style = MaterialTheme.typography.labelSmall,
                                color = VideoPrimary,
                                modifier = Modifier
                                    .background(BadgeBg, shape = BadgeShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                // 视频列表
                items(
                    items = displayList,
                    key = { it.id },
                    contentType = { "video_card" }
                ) { video ->
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
private fun StorageInfoHeader(totalCount: Int, totalSizeBytes: Long) {
    val totalGb = totalSizeBytes / (1024.0 * 1024.0 * 1024.0)

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
            .height(150.dp) // 增加 10dp 高度，给文字留出充足空间
            .clip(CardShape)
            .background(CardBg)
            .clickable(onClick = onClick)
    ) {
        // 缩略图
        ThumbnailPlaceholder(video)

        // 合并显示标题与副标题，减少 UI 节点，性能更优
        val onBgColor = MaterialTheme.colorScheme.onBackground
        val titleStyle = MaterialTheme.typography.bodyMedium.toSpanStyle()
        val subtitleStyle = MaterialTheme.typography.labelSmall.toSpanStyle()

        Text(
            text = remember(video.shortTitle, video.displaySubtitle, onBgColor) {
                buildAnnotatedString {
                    withStyle(titleStyle.copy(color = onBgColor, fontWeight = FontWeight.Medium)) {
                        append(video.shortTitle)
                    }
                    append("\n")
                    withStyle(subtitleStyle.copy(color = VideoOnSurfaceVariant)) {
                        append(video.displaySubtitle)
                    }
                }
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp,
            modifier = Modifier
                .padding(start = 8.dp, top = 4.dp)
                .fillMaxWidth()
                .height(40.dp) // 增加高度，确保两行文字都能显示
        )
    }
}

// ─── Thumbnail ────────────────────────────────────────────────────────────────

@Composable
private fun ThumbnailPlaceholder(video: VideoItem) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // 优化：显式指定 ImageRequest 的 size 和 precision，提升滑动时的响应速度
    val request = remember(video.thumbnailPath) {
        coil.request.ImageRequest.Builder(context)
            .data(video.thumbnailPath)
            .crossfade(false)
            .size(350, 200) // 根据 2 列网格的实际宽度估算
            .precision(coil.size.Precision.INEXACT) // 允许非精确匹配以提高加载速度
            .memoryCacheKey(video.thumbnailPath)
            .diskCacheKey(video.thumbnailPath)
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(VideoSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Coil AsyncImage — 自动缓存、取消、线程管理
        AsyncImage(
            model = request,
            contentDescription = video.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // 时长标签（右下角）：直接在 Text 上应用背景和对齐，减少一个 Box 层级
        Text(
            text = video.formattedDuration,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1, textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .height(30.dp)
                .width(56.dp)
                .padding(0.dp, 0.dp, 8.dp, 6.dp)
                .background(VideoControlBg, shape = BadgeShape)
        )
    }
}
