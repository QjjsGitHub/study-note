package com.example.study.compose

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.study.R
import com.example.study.compose.ui.theme.StudyTheme
import kotlinx.coroutines.delay

const val TAG = "ComposeActivityLOG"

class ComposeActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainUi("Compose")
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUi(name: String, modifier: Modifier = Modifier) {
    StudyTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text("$name 测试应用")
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
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(Icons.Filled.Settings, null)
                        }
                    })
            },
            content = { innerPadding ->
                //垂直排列
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    Text(
                        text = "first $name!", textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )

                    HorizontalDivider(
                        color = Color.Red,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(0.dp, 3.dp, 0.dp, 3.dp)
                    )

                    Text(
                        text = "second $name!", textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    )

                    HorizontalDivider(
                        color = Color.Red,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(0.dp, 3.dp, 0.dp, 3.dp)
                    )

                    Row(
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.girl),
                            contentDescription = "my image",
                            alignment = Alignment.Center,
                            /*
                            滤镜
                            colorFilter = ColorFilter.tint(
                                color = Color.Red,

                                blendMode = BlendMode.SrcIn
                            ),*/
                            modifier = Modifier
                                .height(72.dp)
                                .weight(1.0F, true)
                        )

                        VerticalDivider(
                            color = Color.Red,
                            modifier = Modifier
                                .height(72.dp)
                                .width(1.dp)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.girl),
                            contentDescription = "my image",
                            alignment = Alignment.Center,
                            /*
                            滤镜
                            colorFilter = ColorFilter.tint(
                                color = Color.Red,

                                blendMode = BlendMode.SrcIn
                            ),*/
                            modifier = Modifier
                                .height(72.dp)
                                .weight(1.0F, true)
                        )
                    }

                    HorizontalDivider(
                        color = Color.Red,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .padding(0.dp, 3.dp, 0.dp, 3.dp)
                    )

                    Spacer(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth()
                    )

                    MyTextField()

                    Spacer(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth()
                    )

                    TextFieldValuePreview()

                    //val users = listOf(User(), User(), User(), User(), User(), User()) // 示例数据
                    val users = remember {
                        mutableStateListOf<User>().apply {
                            for (i in 0..100) {
                                add(User(name = "人机 $i"))
                            }
                        }
                    }

                    ListView(users, modifier)
                }
            }
        )
    }
}


data class User(val name: String = "小明", var age: Int = 18, var image: Int = 1) {

    fun getInfo(): String {
        return "我是 " + age + "岁的" + name + " 头像是:" + image
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListView(users: SnapshotStateList<User>, modifier: Modifier) {

    val context = LocalContext.current // 获取上下文，用于可能的网络请求等

    val isRefreshing = false

    val onRefresh = {
        for (i in 0..<users.size) {
            if (i % 2 == 0)
                users[i] = User(users[i].name, users[i].age + 1, 2)
        }

        // users.add()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {

        LazyColumn(
            modifier = modifier
                .fillMaxHeight()
                .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            val height = 60.dp

            val modifier1 = Modifier
                .height(height)
                .width(height)
                .wrapContentHeight(Alignment.CenterVertically)
            val modifier2 = Modifier
                .fillMaxHeight()
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(16.dp, 0.dp, 0.dp, 0.dp)
            val modifier3 = Modifier
                .fillMaxHeight()
                .wrapContentHeight(Alignment.CenterVertically)
                .padding(8.dp, 0.dp, 0.dp, 0.dp)

            stickyHeader {
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .zIndex(1.0f)
                ) {

                    Text(
                        text = "头像", textAlign = TextAlign.Center,
                        modifier = Modifier
                            .height(32.dp)
                            .width(height)
                            .wrapContentHeight(Alignment.CenterVertically)
                    )
                    Text(
                        text = "姓名", textAlign = TextAlign.Center,
                        modifier = modifier2.weight(3.0f)
                    )
                    Text(
                        text = "年龄", textAlign = TextAlign.Start,
                        modifier = modifier3.weight(1.0f)
                    )

                }
            }

            items(users.size, key = { i -> users[i].name }) { item ->
                val user = users[item]

                /*val dismissState = rememberSwipeToDismissBoxState(
                SwipeToDismissBoxValue.Settled,
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.EndToStart) {
                        users.remove(user)
                        true
                    } else {
                        false
                    }
                }, SwipeToDismissBoxDefaults.positionalThreshold
            )*/

                val dismissState = rememberSwipeToDismissBoxState(
                    SwipeToDismissBoxValue.Settled,
                    SwipeToDismissBoxDefaults.positionalThreshold
                )

                SwipeToDismissBox(
                    modifier = Modifier.animateItem(),
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> Color.Red
                            else -> Color.Transparent
                        }

                        /*Log.d(
                        TAG,
                        "dismissDirection" + dismissState.dismissDirection + ":" +
                                dismissState.progress + ":" + dismissState.currentValue + ":" + dismissState.targetValue
                    )*/

                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart && dismissState.progress >= 1.0f) {
                            users.remove(user)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.visible(
                                    dismissState.dismissDirection
                                            == SwipeToDismissBoxValue.EndToStart
                                ),
                                tint = Color.White
                            )
                        }
                    }
                ) {


                    Row(
                        modifier = Modifier
                            .height(height)
                            .fillMaxWidth()
                            .clickable(onClick = {

                                //val (name, age, image1) = user

                                Toast.makeText(
                                    context,
                                    user.getInfo(),
                                    Toast.LENGTH_SHORT
                                ).show()

                            })
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (user.image == 1) R.drawable.girl else R.drawable.girl2
                            ),
                            contentDescription = "my image",
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop,
                            modifier = modifier1
                        )
                        Text(
                            text = "名字：" + user.name, textAlign = TextAlign.Center,
                            modifier = modifier2.weight(3.0f)
                        )
                        Text(
                            text = user.age.toString() + "岁", textAlign = TextAlign.Start,
                            modifier = modifier3.weight(1.0f)
                        )
                    }

                }
            }

            stickyHeader {
                Text(
                    text = "没了", textAlign = TextAlign.Center,
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                )
            }

        }
    }


}

@Composable
fun MyTextField() {
    var text by remember { mutableStateOf("") }// 定义state对象：text ，并设为全局
    TextField(
        value = text,//text 与TextField进行绑定
        onValueChange = {
            if (it != "请输入")
                Log.d(TAG, "输入: $it")
        },
        //当输入框值发生变换时，改变text值，从而引起状态的刷新，进而重组
        label = { Text("请输入") }//提示
        , modifier = Modifier.wrapContentWidth(),
        textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 14.sp)

    )
}


@Composable
fun TextFieldValuePreview(
) {
    val textFieldValueState = remember {
        mutableStateOf(
            TextFieldValue(
                annotatedString = buildAnnotatedString {
                    append("小明")

                    withStyle(
                        style = SpanStyle(
                            color = Color.Yellow,
                            //设置阴影
                            shadow = Shadow(
                                color = Color.Blue,//阴影颜色
                                blurRadius = 3f,//虚化
                            )
                        )
                    ) {
                        append("你好")
                    }
                },
                selection = TextRange(3)// 光标默认显示在第二个字符位置
            )
        )
    }

    val showKeyboard = remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }
    //val keyboard = LocalSoftwareKeyboardController.current

    // 显示键盘
    LaunchedEffect(focusRequester, block = {
        if (showKeyboard.value) {

            delay(1000)

            focusRequester.requestFocus()

            Log.d(
                TAG,
                "currentThread: " + Thread.currentThread().name + "mainThread: " + Looper.getMainLooper().thread.name
            )

            //delay(3000)
            //输入框获得焦点就会显示软键盘，强制显示软键盘如果没有输入焦点，软键盘不会真正显示
            //keyboard?.show()
        }

    })

    TextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .wrapContentHeight(),
        value = textFieldValueState.value,
        onValueChange = {
            textFieldValueState.value = it
        }
    )


}

@Preview(showBackground = true)
@Composable
fun MainUiPreview() {
    MainUi("Compose")
}