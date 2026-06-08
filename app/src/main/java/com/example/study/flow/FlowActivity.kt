package com.example.study.flow

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.study.compose.ui.theme.StudyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.timeout
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds


const val TAG: String = "FlowActivityLOG"

/**
 *
 * 热流（Hot Stream）：启动后就会产生数据，即使没有 collect() 也会持续运行。
 * 支持多生产者-多消费者（类似消息队列）。
 * 需要手动关闭，否则会一直等待数据。
 * 适用于高频数据流，但 默认不支持背压（可以用 BufferedChannel 处理）。
 * val channel = Channel<Int>()
 *
 * lifecycleScope.launch {
 *     for (i in 1..3) {
 *         delay(200)
 *         channel.send(i) // 发送数据
 *     }
 *     channel.close() // 需要手动关闭
 * }
 *
 * lifecycleScope.launch {
 *     for (value in channel) {
 *         println("Received: $value")
 *     }
 * }
 * 运行项目并下载源码
 * Kotlin
 * 运行
 *
 * 10.2 RxJava（响应式编程）
 *
 * 功能丰富（map()、flatMap()、combineLatest()、throttle()）。
 * 支持背压（Flowable）。
 * 支持线程切换（observeOn()、subscribeOn()）。
 * 支持生命周期管理（CompositeDisposable）。
 * val observable = Observable.create<Int> { emitter ->
 *     for (i in 1..3) {
 *         Thread.sleep(200) // 模拟数据产生
 *         emitter.onNext(i) // 发送数据
 *     }
 *     emitter.onComplete()
 * }
 *
 * observable
 *     .subscribeOn(Schedulers.io()) // 切换线程
 *     .observeOn(AndroidSchedulers.mainThread()) // 观察线程
 *     .subscribe { value ->
 *         println("Received: $value")
 *     }
 * 运行项目并下载源码
 * Kotlin
 * 运行
 *
 * RxJava概括起来说，就是很牛，他的功能绝不是我上面的介绍的Flow就能概括的，但是API有点复杂，生命周期还需要自己去管理，用了一两个项目后就用协程，viewmodel这些东西了。
 *
 */

//背压（Backpressure） 是指在数据流中，数据生产速度大于消费速度 时，如何处理过量的数据的问题。
//如果 数据生产者（Producer） 太快，而 数据消费者（Consumer） 处理不过来，就会导致 OOM（内存溢出） 或 数据丢失。


class FlowActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val vm: FlowViewModel by viewModels {
            object : ViewModelProvider.Factory {

                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FlowViewModel() as T
                }

            }
        }

        setContent {
            StudyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainUI(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        vm
                    )
                }
            }
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
fun MainUI(name: String, modifier: Modifier = Modifier, vm: FlowViewModel) {

    val myFlow = flow {


        for (i in 1..80) {
            delay(50)
            emit(i.toFloat())
        }

        for (i in 81..94) {
            delay(100)
            emit(i.toFloat())
        }

        for (i in 95..99) {
            delay(200)
            emit(i.toFloat())
        }
        for (i in 1..100) {
            delay(50)
            emit(99 + i.toFloat() / 100)
        }

    }


    //val l= LocalView
    val lifecycleOwner = LocalLifecycleOwner.current

    var progress by remember { mutableFloatStateOf(0.6f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500)
    )

    Column(
        modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(0.dp, 16.dp, 0.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // verticalArrangement = Alignment.CenterVertically as Arrangement.Vertical
    ) {

        /*Row() {

            Text("11")
            Text("11")
            Text("11")

        }*/

        CircularProgressIndicator(
            progress = { progress.absoluteValue / 100 },
            modifier = Modifier
                .width(60.dp)
                .height(60.dp),
            color = Color.Red,
            strokeWidth = 3.dp,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = StrokeCap.Round,
        )

        Spacer(Modifier.height(16.dp))


        LaunchedEffect(Dispatchers.IO) {


            myFlow.timeout(15000.milliseconds).flowOn(Dispatchers.IO)
                .catch { e -> // ✅ 只会捕获 Flow emit() 的异常
                    println("Caught exception: ${e.message}")
                }.retryWhen { e, attempt -> // attempt: 第几次重试
                    println("Retrying... attempt $attempt")
                    attempt < 2 // ✅ 只重试 3 次
                }.buffer() //让生产者继续工作，并缓存数据 conflate()继续工作丢弃旧数据
                .collect {

                    Log.d(TAG, it.toString())
                    progress = it

                }

            /*collectLatest - 取消上一个，处理最新值

            每次有新数据到来，取消上一个任务，只处理最新的。
            适用于耗时操作，避免处理过时数据。*/
        }


        Text(
            text = "Hello $name!",
            modifier = Modifier
        )

        Spacer(Modifier.height(16.dp))

        var text1 by remember { mutableStateOf("") }

        val query by vm.query.collectAsState()
        val result by vm.searchResult.collectAsState()


        TextField(
            value = text1,
            onValueChange = { text1 = it },
            label = { Text("请输入内容") }
        )


        // 搜索框
        OutlinedTextField(
            value = query,
            onValueChange = { vm.query.value = it },
            label = { Text("搜索") },
            modifier = Modifier.fillMaxWidth()
        )

        // 搜索结果
        result.forEach {
            Text(text = it)
        }

    }

}

@Preview(showBackground = true)
@Composable
fun MainUIPreview() {
    StudyTheme {
        MainUI("Android", Modifier, FlowViewModel())
    }
}