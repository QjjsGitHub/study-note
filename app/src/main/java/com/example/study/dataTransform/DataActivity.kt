package com.example.study.dataTransform

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import coil.disk.DiskCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.study.R
import com.example.study.dataTransform.ui.theme.StudyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

const val TAG: String = "DataActivityLOG"

class DataActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StudyTheme {
                MainUI()
            }
        }

        /*CoroutineScope(Dispatchers.IO).launch {
            var f = getDir("myDir1", Context.MODE_PRIVATE)
            var b = f.mkdirs()
            var l = f.exists()

        }*/
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI(modifier: Modifier = Modifier) {

    // 使用状态栏高度 例如在布局中使用
    //val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val context = LocalContext.current

    val mySpace = rememberCoroutineScope()

    val textValue1: MutableState<Pair<Int, String>> = remember { mutableStateOf(0 to "null") }


    LaunchedEffect(Unit) {
        Log.d(TAG, "default thread: " + Thread.currentThread().name)
        withContext(Dispatchers.IO) {
            Log.d(TAG, "io thread: " + Thread.currentThread().name)
            Log.d(TAG, "name: " + textValue1.value.second + "age: " + textValue1.value.first)
            textValue1.value =
                DataTool.getInstance(WeakReference(context)).sharePreferencesTest()
        }
        Log.d(TAG, "name: " + textValue1.value.second + " age: " + textValue1.value.first)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("数据测试")
                }, navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(Icons.Filled.Menu, null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(Icons.Filled.Share, null)
                    }
                })
        }, content = { innerPadding ->

            Column(modifier = modifier.padding(innerPadding)) {

                Text(
                    text = "name: " + textValue1.value.second + " age: " + textValue1.value.first,
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                val title = remember { mutableStateOf("") }
                val data = remember { mutableStateOf("") }
                val content = remember { mutableStateOf("") }
                val id = remember { mutableStateOf("") }

                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                )
                {

                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1.0f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {

                        Button(onClick = {
                            mySpace.launch {

                                DataTool.getInstance(WeakReference(context)).testContentProvider(
                                    context,
                                    1,
                                    if (id.value.isNotEmpty()) id.value.toInt() else 1,
                                    title.value,
                                    data.value.toIntOrNull() ?: 0,
                                    content.value
                                )
                            }
                        }) {
                            Text("插入数据")
                        }

                        Button(onClick = {
                            mySpace.launch {
                                withContext(Dispatchers.IO) {
                                    DataTool.getInstance(WeakReference(context))
                                        .testContentProvider(
                                            context,
                                            2,
                                            if (id.value.isNotEmpty()) id.value.toInt() else 1,
                                            title.value,
                                            data.value.toIntOrNull() ?: 0,
                                            content.value
                                        )
                                }
                            }
                        }) { Text("查数据") }


                        Button(onClick = {
                            mySpace.launch {
                                withContext(Dispatchers.IO) {
                                    DataTool.getInstance(WeakReference(context))
                                        .testContentProvider(
                                            context,
                                            3,
                                            if (id.value.isNotEmpty()) id.value.toInt() else 1,
                                            title.value,
                                            data.value.toIntOrNull() ?: 0,
                                            content.value
                                        )
                                }
                            }
                        }) { Text("查单一数据") }

                    }


                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1.0f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        Button(onClick = {
                            mySpace.launch {
                                withContext(Dispatchers.IO) {
                                    DataTool.getInstance(WeakReference(context))
                                        .testContentProvider(
                                            context,
                                            4,
                                            if (id.value.isNotEmpty()) id.value.toInt() else 1,
                                            title.value,
                                            data.value.toIntOrNull() ?: 0,
                                            content.value
                                        )
                                }
                            }
                        }) { Text("改单一数据") }

                        Button(onClick = {
                            mySpace.launch {
                                withContext(Dispatchers.IO) {
                                    DataTool.getInstance(WeakReference(context))
                                        .testContentProvider(
                                            context,
                                            5,
                                            if (id.value.isNotEmpty()) id.value.toInt() else 1,
                                            title.value,
                                            data.value.toIntOrNull() ?: 0,
                                            content.value
                                        )
                                }
                            }
                        }) { Text("改数据") }
                        Button(onClick = {
                            mySpace.launch {
                                withContext(Dispatchers.IO) {
                                    DataTool.getInstance(WeakReference(context))
                                        .testContentProvider(
                                            context,
                                            6,
                                            if (id.value.isNotEmpty()) id.value.toInt() else 1,
                                            title.value,
                                            data.value.toIntOrNull() ?: 0,
                                            content.value
                                        )
                                }
                            }
                        }) { Text("删除单一数据") }
                        Button(onClick = {
                            mySpace.launch {
                                withContext(Dispatchers.IO) {
                                    DataTool.getInstance(WeakReference(context))
                                        .testContentProvider(
                                            context,
                                            7,
                                            if (id.value.isNotEmpty()) id.value.toInt() else 1,
                                            title.value,
                                            data.value.toIntOrNull() ?: 0,
                                            content.value
                                        )
                                }
                            }
                        }) { Text("删除数据") }
                    }

                }



                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(0.dp, 16.dp, 0.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {

                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1.0f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {

                        Box(
                            modifier = Modifier
                                .height(60.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "id",
                                modifier = Modifier
                                    .wrapContentHeight(),
                                style = TextStyle(textAlign = TextAlign.Center),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .height(60.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "title", Modifier.wrapContentHeight(),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .height(60.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "data", Modifier.wrapContentHeight(),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .height(60.dp), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "content", Modifier.wrapContentHeight(),
                                style = TextStyle(textAlign = TextAlign.Center)
                            )
                        }

                    }

                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(3.0f)
                            .padding(0.dp, 0.dp, 36.dp, 0.dp)
                    )
                    {
                        TextField(
                            value = id.value,
                            modifier = Modifier
                                .wrapContentHeight(),
                            onValueChange = { newText ->
                                // 只允许输入数字，允许删除字符（使用.filterfilter）
                                id.value = newText.filter { it.isDigit() }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(12.dp))
                        TextField(
                            value = title.value,
                            modifier = Modifier
                                .wrapContentHeight(),
                            onValueChange = { title.value = it })
                        Spacer(Modifier.height(12.dp))
                        TextField(
                            value = data.value,
                            modifier = Modifier
                                .wrapContentHeight(),
                            onValueChange = { newText ->
                                // 只允许输入数字，允许删除字符（使用filter）
                                data.value = newText.filter { it.isDigit() }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(12.dp))
                        TextField(
                            value = content.value,
                            modifier = Modifier
                                .wrapContentHeight(),
                            onValueChange = { content.value = it })
                    }

                }


                val imagePath = remember { mutableStateOf("") }
                val imagePaths = remember { mutableStateOf<Array<String>>(arrayOf()) }
                val sliderPosition = remember { mutableFloatStateOf(0f) } // 初始位置

                val imageLoader = remember {
                    ImageLoader.Builder(context.applicationContext)
                        .memoryCachePolicy(CachePolicy.DISABLED) // 启用内存缓存
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCache {
                            DiskCache.Builder()
                                .directory(
                                    context.applicationContext.getExternalFilesDir("myDir")
                                        ?.resolve("coil_cache") ?: context.applicationContext.getDir(
                                        "myDir",
                                        Context.MODE_PRIVATE
                                    ).resolve(
                                        "coil_cache"
                                    )
                                )
                                .maxSizeBytes(100L * 1024 * 1024)   // 磁盘缓存 100MB
                                .build()
                        }
                        .build()
                }

                val painter =
                    rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context.applicationContext).data(imagePath.value)
                            .crossfade(true).size(100, 100)
                            .build(),
                        imageLoader = imageLoader
                    )


                Button(onClick = {

                    mySpace.launch {
                        imagePaths.value = withContext(Dispatchers.IO) {
                            DataTool.getInstance(
                                WeakReference(
                                    context
                                )
                            ).getImagesNew()
                        }
                    }

                }) {
                    Text("获取图片")
                }


                /*LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    color = ProgressIndicatorDefaults.linearColor,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                ) // 0.5f 表示50%的进度*/


                Slider(

                    modifier = modifier.height(48.dp),

                    value = sliderPosition.floatValue,
                    onValueChange = {
                        sliderPosition.floatValue = it
                        //Log.d(TAG, "Slider onValueChange: $it")
                    }, // 当值改变时更新sliderPosition
                    valueRange = 0f..1f, // 设置滑动范围
                    onValueChangeFinished = {

                        if (!imagePaths.value.isEmpty()) {
                            val i = (sliderPosition.floatValue * imagePaths.value.size).toInt()

                            imagePath.value = imagePaths.value[i]

                            Log.d(TAG, "Slider onValueChangeFinished: $i : $imagePath.value ")
                        }

                    }//可以在这里处理滑动结束后的操作
                )

                val bitmap = remember {
                    try {
                        BitmapFactory.decodeFile("/storage/emulated/0/DCIM/Camera/IMG_20240502_164103.jpg")
                    } catch (_: Exception) {
                        null
                    }
                }

                Row(modifier.fillMaxWidth())
                {

                    AsyncImage(
                        model = ImageRequest.Builder(context.applicationContext)
                            .data("/storage/emulated/0/DCIM/Camera/IMG_20240502_164103.jpg")
                            .crossfade(true)                    // 启用交叉淡入淡出
                            .placeholder(R.drawable.ic_home_black_24dp) // 占位图资源
                            .error(R.drawable.ic_notifications_black_24dp)             // 错误图资源
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = "高级配置示例",
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f)
                    )

                    Image(
                        painter = if (imagePath.value != "") {
                            painter
                        } else {
                            bitmap?.asImageBitmap()?.let { BitmapPainter(it) }
                                ?: painterResource(R.drawable.ic_home_black_24dp)
                        },
                        contentDescription = "my image",
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f)
                    )


                    AsyncImage(
                        model = "https://www.baidu.com/img/PCfb_5bf082d29588c07f842ccde3f97243ea.png",
                        imageLoader = imageLoader,
                        contentDescription = "带状态反馈的图片",
                        placeholder = painterResource(R.drawable.ic_home_black_24dp),
                        error = painterResource(R.drawable.ic_notifications_black_24dp),
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                    )


                    val url = "https://www.baidu.com/img/PCfb_5bf082d29588c07f842ccde3f97243ea.png"
                    val painter1 = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context.applicationContext)
                            .data(url)
                            .build(),
                        imageLoader = imageLoader
                    )
                    SubcomposeAsyncImage(
                        model = url,
                        imageLoader = imageLoader,
                        contentDescription = "自定义状态图片",
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f)
                    ) {
                        when (val state = painter1.state) {
                            is AsyncImagePainter.State.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            is AsyncImagePainter.State.Error -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "加载失败: ${state.result.throwable.message}",
                                        color = Color.Red
                                    )
                                }
                            }

                            else -> {
                                SubcomposeAsyncImageContent()
                            }
                        }
                    }

                }

            }

        })


}

@Preview(showBackground = true)
@Composable
fun MainUIPreview() {
    StudyTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainUI(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}