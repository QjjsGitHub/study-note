package com.example.study.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.study.R
import com.example.study.ui.theme.MyTheme
import kotlinx.coroutines.delay
import java.util.Objects

class NormalActivity : AppCompatActivity() {

    val TAG = "NormalActivity"
    var handle: Handler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //无边框
        enableEdgeToEdge()

        setContent {
            MyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {

                    Column(
                        modifier = Modifier.height(300.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,//内容组件水平居中
                        verticalArrangement = Arrangement.SpaceBetween//内容组件垂直分布到两侧
                    ) {

                        Row(
                            modifier = Modifier
                                .height(72.dp)
                                .weight(1f),
                            horizontalArrangement = Arrangement.Start,//内容组件对齐
                            verticalAlignment = Alignment.CenterVertically//内容组件垂直居中
                        ) {

                            Hello("啊啊啊啊啊 1111")
                            VerticalDivider(
                                color = Color.Yellow,
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(400.dp)//宽度为1dp
                            )
                            MyText1()
                            VerticalDivider(
                                color = Color.Yellow,
                                modifier = Modifier
                                    .height(20.dp)
                                    .fillMaxWidth()
                            )
                            MyText2()
                        }

                        HorizontalDivider(
                            color = Color.Red,
                            modifier = Modifier
                                .height(10.dp)
                                .fillMaxWidth()
                                .weight(1f),
                        )

                        Row(
                            modifier = Modifier
                                .height(36.dp)
                                .weight(1f),
                            horizontalArrangement = Arrangement.Start,//内容组件对齐
                            verticalAlignment = Alignment.CenterVertically//内容组件垂直居中
                        ) {

                            Box(
                                modifier = Modifier
                                    .sizeIn(
                                        200.dp,
                                        200.dp
                                    ),//设置内容组件的最小宽度和高度为50dp、70dp,配合propagateMinConstraint=true使用
                                //propagateMinConstraints = true,//使内容组件最小宽度和高度生效
                            ) {
                                // propagateMinConstraints，内部需要一个组件撑大整体的大小
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(Color.Blue)
                                )
                                MyImage(300)
                                Image(
                                    modifier = Modifier.size(80.dp, 80.dp),
                                    alignment = Alignment.BottomEnd,
                                    painter = painterResource(id = R.drawable.ic_menu_slideshow),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillHeight // 图片高度拉伸
                                )
                            }

                        }

                        HorizontalDivider(
                            color = Color.Yellow,
                            modifier = Modifier
                                .height(20.dp)
                                .fillMaxWidth()
                                .weight(1f),
                        )


                        Row(
                            modifier = Modifier
                                .height(36.dp)
                                .weight(1f),
                            horizontalArrangement = Arrangement.Start,//内容组件对齐
                            verticalAlignment = Alignment.CenterVertically//内容组件垂直居中
                        ) {

                            Spacer(modifier = Modifier.width(20.dp))
                            MyTextField()
                            Spacer(modifier = Modifier.width(20.dp))
                            TextFieldValuePreview()

                        }


                    }


                }


            }
        }

        /*val binding = ActivityNormalBinding.inflate(layoutInflater)
        setContentView(binding.root)*/

        /*ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/


        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), onApplyWindowInsets(v, insets));
        val handler1: Handler = object : Handler(
            Looper.myLooper() ?: Looper.getMainLooper(),
            Callback {
                if (it.what == 1) {
                    Log.d(TAG, "捕获到what=1的message")
                    true
                } else
                    false
            }) {
            override fun handleMessage(msg: Message) {
                if (msg.what == 1)
                    Log.d(TAG, "捕获到what=1的message")

            }
        }

        val handler = Objects.requireNonNull(Looper.myLooper())?.let {
            Handler(
                it
            ) { false }
        }

        handle = Looper.myLooper()?.let { looper ->
            Handler(
                looper
            ) {
                //捕获到what=1的message
                if (it.what == 1) {
                    Log.d(TAG, "捕获到what=1的message")
                    true
                } else
                    false
            }
        }


        var message: Message = Message.obtain(handle, Runnable {
            Log.d(TAG, "")
            Log.d(TAG, "")
        })


        handle?.sendMessage(message)

        handle?.postDelayed({ Log.d(TAG, "handle-message") }, 5000)

        var intent1: Intent = intent
        var bundle: Bundle? = intent1.getBundleExtra("test")

        Log.d("NormalActivity", "NormalActivityAge" + (bundle?.getInt("age") ?: ""))

        /*
        运算符
         */
        fun 数据类型() {

            /* 整数
            类型	大小 (bits)	最小值	最大值
            Byte	8	-128	127
            Short	16	-32768	32767
            Int	32	-2,147,483,648 (-231)	2,147,483,647 (231 - 1)
            Long	64	-9,223,372,036,854,775,808 (-263)	9,223,372,036,854,775,807 (263 - 1)*/

            val one = 1 // Int
            val threeBillion = 3000000000 // Long
            val oneLong = 1L // Long
            val oneByte: Byte = 111

            /* 类型	大小 (bits)	有效位数（Significant bits）	指数位数（Exponent bits）	十进制数字（Decimal digits）
            Float	32	24	8	6-7
            Double	64	53	11	15-16*/

            val pi = 3.14 // Double
            //val one1: Double = 1 // Error: type mismatch
            val oneDouble = 1.0 // Double
            val e = 2.7182818284 // Double
            val eFloat = 2.7182818284f
            // Float, 2.7182817 ,如果小数位数超长,则进行四舍五入(这是因为计算机在存储浮点数时，
            // 使用的是二进制（base-2）表示法，而不是我们常用的十进制（base-10）表示法。
            // 在二进制表示法中，无法精确地表示大多数十进制小数。)

            //  kotlin数据类型不支持隐式转换，也就是说不能把一个较小的值自动转换成较大类型，需要手动转换才可以

            fun printDouble(d: Double) {
                print(d)
            }

            val i = 1
            val d = 1.0
            val f = 1.0f

            printDouble(d)
            //    printDouble(i) // Error: Type mismatch
            //    printDouble(f) // Error: Type mismatch
            printDouble(i.toDouble())
            printDouble(f.toDouble())
        }

        fun 运算符() {

            //? 变量可为空
            //?. 忽略空异常
            var bundle: Bundle? = null
            if (Math.random().equals(1.11)) {
                bundle = Bundle()
            }
            //?: 前者为空则取后者
            val newAge: Int = bundle?.getInt("age") ?: 1

            //!!加在变量后面，表示该变量如果为null时，会抛出空指针异常，像java语法一样空指针不安全；
            //如果不为null，才会正常执行该变量后面的内容。
            try {
                bundle!!.getInt("age")
            } catch (e: NumberFormatException) {
                println("")
            }

            //在Kotlin中，=== 表示比较对象地址，== 表示比较两个值大小。
            //所以无论是 a == a 还是 a === a 都是返回true，因为是同一个变量，数值大小和地址都是相等的。

            fun(): Unit {
                val a: Int = 100   //a其实就是个数值, 不涉及装箱的问题, 也就不是对象。
                println(a == a) //true
                println(a === a) //true
                val a1: Int = a
                val a2: Int = a
                println(a1 == a2) //true
                println(a1 === a2) //true

            }

            fun(): Unit {

                val a: Int = 1000
                println(a == a) //true
                println(a === a) //true
                val a1: Int? = a  //a1是一个Int型对象, 因为它涉及到装箱问题。
                val a2: Int? = a
                println(a1 == a2) //true
                println(a1 === a2) //false

            }

            fun(): Unit {

                val a: Int? = 1000  //a是一个Int型对象, 因为它涉及到装箱问题。
                println(a == a) //true
                println(a === a) //true
                val a1: Int? = a  //不再需要创建
                val a2: Int? = a
                println(a1 == a2) //true
                println(a1 === a2) //true

            }

            fun(): Unit {
                val a: Int = 12   //在范围是 [-128, 127] 之间的数装箱时并不会创建新的对象
                println(a == a) //true
                println(a === a) //true
                val a1: Int? = a
                val a2: Int? = a
                println(a1 == a2) //true
                println(a1 === a2) //true

            }

            //->  函数类型 (R, T) -> R，该函数接受类型分别为 R 与 T 的两个参数并返回一个 R 类型的值


        }

        /**
         * 有参数有返回方式2
         */
        fun sum1(a: Int, b: Int): Int {
            println("这是一个有参数有返回的方法书写方式2")
            return a + b
        }

        /**
         * 有参数无返回值方式1
         */
        fun sum2(a: Int, b: Int): Unit {
            println("这是一个有参数无返回的方法书写方式1，参数1=0" + a + "/参数2=" + b)
        }

        /**
         * 有参数无返回值方式2
         */
        fun sum3(a: Int, b: Int) {
            println("这是一个有参数无返回的方法书写方式2，参数1=0" + a + "/参数2=" + b)
        }

        /**
         * 无参数无返回值方式1
         */
        fun sum4() {
            println("这是一个无参方法")
        }


        /**
         * 判断数据类型
         */
        fun checkType(a: Any) {
            if (a is String) {
                println("该数据是字符串类型")
            } else if (a is Int) {
                println("该数据是整数类型")
            } else if (a is Double) {
                println("该数据是小数类型")
            } else if (a is Float) {
                println("该数据是浮点类型")
            } else {
                println("该数据是其他的类型")
            }
        }

        /**
         * for 循环
         */

        fun forErch() {
            var list = ArrayList<String>()

            var a = IntArray(5)
            list = arrayOf("", "").asList() as ArrayList<String>
            //
            //            Array<String>(1);

            val lists = listOf<String>("110", "200", "啥也不是")
            for (l in lists) {
                println("for循环集合元素=" + l)
            }
        }

        /**
         * while 循环
         */
        fun whileErch() {
            val lists = listOf<String>("元素1", "元素2", "元素3")
            var index = 0
            while (index < lists.size) {
                //  println("item at $index is ${lists[index]}")
                println(lists[index])
                index++
            }
        }

        /**
         * 类型转换
         */
        fun changeType(a: Any): Int? {

            if (a is String) {
                return a.length
            }
            return null
        }

        /**
         * when表达式
         */
        fun whenType(a: Any) {
            when (a) {
                1 -> println("参数是整数类型")
                "1" -> println("参数是字符串类型")
                1.0 -> println("参数是小数类型")
                1.00f -> println("参数是浮点数数类型")
                true -> println("参数是布尔类型")
                else -> println("参数是未知类型")
            }
        }

    }

    override fun onDestroy() {
        //handle.removeCallbacksAndMessages(null)
        super.onDestroy()
    }


    @Composable
    fun Hello(s: String, modifier: Modifier = Modifier.height(20.dp)) {
        Text(
            text = "say $s?", modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MyTheme {
            /*    Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    hello("Hello")
                }*/
            Hello("Hello 1111")
        }

    }

    @Composable
    @Preview
    fun MyText1() {
        Text(text = stringResource(id = R.string.say_hi), modifier = Modifier.height(30.dp))
    }

    @Preview
    @Composable
    fun MyText2(modifier: Modifier = Modifier.height(50.dp)) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = ParagraphStyle(
                        lineHeight = 18.sp,//行高
                        textAlign = TextAlign.Left,//左对齐
                        textIndent = TextIndent(firstLine = 10.sp)//缩进
                    )
                ) {
                    withStyle(
                        style = SpanStyle(
                            fontSize = 12.sp,
                            color = Color.Red,//设置颜色为红色
                            fontWeight = FontWeight.Medium//加粗
                        )
                    ) {
                        append("hii\n")
                    }
                }

                withStyle(
                    style = ParagraphStyle(
                        lineHeight = 18.sp,
                    )
                ) {
                    withStyle(
                        style = SpanStyle(
                            fontSize = 10.sp,
                            color = Color.Yellow,
                            shadow = Shadow(//设置阴影
                                color = Color.Blue,//阴影颜色
                                blurRadius = 3f,//虚化
                                offset = Offset(5f, 20f)//x,y轴的偏移
                            )
                        )
                    ) {
                        append("拼接\n")
                    }
                }
            },
            modifier = modifier
        )
    }

    @Composable
    fun MyImage(size: Int) {
        Image(
            painter = painterResource(id = R.drawable.girl),
            contentDescription = "my image",
            alignment = Alignment.CenterStart,
            colorFilter = ColorFilter.tint(
                color = Color.Red,

                blendMode = BlendMode.SrcIn
            ), modifier = Modifier
                .height(size.dp)
                .width((size + 10).dp)
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun MyTextField() {
        var text by remember { mutableStateOf("") }// 定义state对象：text ，并设为全局
        TextField(
            value = text,//text 与TextField进行绑定
            onValueChange = {
                text = it
            },//当输入框值发生变换时，改变text值，从而引起状态的刷新，进而重组
            label = { Text("hint") }//提示
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Preview
    @Composable
    fun TextFieldValuePreview(
    ) {
        val textFieldValueState = remember {
            mutableStateOf(
                TextFieldValue(
                    annotatedString = buildAnnotatedString {
                        append("hi")

                        withStyle(
                            style = SpanStyle(
                                color = Color.Red,
                                //设置阴影
                                shadow = Shadow(
                                    color = Color.Blue,//阴影颜色
                                    blurRadius = 3f,//虚化
                                )
                            )
                        ) {
                            append("你好\n")
                        }
                    },
                    selection = TextRange(2)// 光标默认显示在第二个字符位置
                )
            )
        }

        val showKeyboard = remember { mutableStateOf(true) }
        val focusRequester = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current

        // 显示键盘
        LaunchedEffect(focusRequester) {
            if (showKeyboard.value) {
                focusRequester.requestFocus()
                delay(100)
                keyboard?.show()
            }
        }

        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            value = textFieldValueState.value,
            onValueChange = {
            }
        )
    }

}