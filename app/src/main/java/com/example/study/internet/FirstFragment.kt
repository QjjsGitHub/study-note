package com.example.study.internet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.study.R
import com.example.study.databinding.FragmentFirstBinding
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val mCompositeDisposable = CompositeDisposable()
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    class User(
        var id: Int,
        var name: String,
        var isActive: Boolean
    ) {
        /*var id = id
        var name = name
        var isActive = isActive*/

        constructor (id: Int, name: String) : this(id, name, true)

    }

    interface ApiService {
        // 以Observable为例，返回用户列表
        @GET("s?tn=68018901_7_oem_dg&ie=UTF-8&wd=你好")
        fun getUsers(): Observable<List<User>>
    }

    val retrofit = Retrofit.Builder()
        .baseUrl("https://www.baidu.com/")  // 替换为实际的baseUrl
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 适配器，将返回类型转换为Observable
        .build()

    val apiService = retrofit.create(ApiService::class.java)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }


    fun getDataOnLocal() {

        val observable = Observable.create<Int?>(object : ObservableOnSubscribe<Int?> {
            @Throws(Exception::class)
            override fun subscribe(e: ObservableEmitter<Int?>) {
                for (i in 0..99) {
                    if (i % 20 == 0) {
                        try {
                            Thread.sleep(500) //模拟下载的操作。
                        } catch (exception: InterruptedException) {
                            if (!e.isDisposed()) {
                                e.onError(exception)
                            }
                            Log.d(
                                InternetActivity.INTERNET_ACTIVITY_TAG, "onError Thread id: " +
                                        Thread.currentThread().id
                            )
                        }
                        Log.d(
                            InternetActivity.INTERNET_ACTIVITY_TAG, "onNext Thread id: " +
                                    Thread.currentThread().id
                        )
                        e.onNext(i)
                    }
                }
                Log.d(
                    InternetActivity.INTERNET_ACTIVITY_TAG, "onComplete Thread id: " +
                            Thread.currentThread().id
                )

                e.onComplete()
            }
        })
        val disposableObserver: DisposableObserver<Int?> = object : DisposableObserver<Int?>() {
            override fun onNext(value: Int) {
                Log.d(
                    InternetActivity.INTERNET_ACTIVITY_TAG,
                    "onNext=" + value + " Thread id:" + Thread.currentThread().id
                )
                binding.textviewFirst.setText("Current Progress=" + value)
            }

            override fun onError(e: Throwable) {
                Log.d(
                    InternetActivity.INTERNET_ACTIVITY_TAG,
                    "onError=" + e + " Thread id:" + Thread.currentThread().id
                )
                binding.textviewFirst.setText("Download Error")
            }

            override fun onComplete() {
                Log.d(
                    InternetActivity.INTERNET_ACTIVITY_TAG,
                    "onComplete" + " Thread id:" + Thread.currentThread().id
                )
                binding.textviewFirst.setText("Download onComplete")
            }
        }
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(disposableObserver)

        mCompositeDisposable.add(disposableObserver)

    }

    fun getData() {

        /*// 发起网络请求，并在IO线程执行请求，在主线程更新UI
        val disposable: Disposable = apiService.getUsers()
            .subscribeOn(Schedulers.io()) // 网络请求放在IO线程执行
            .observeOn(AndroidSchedulers.mainThread()) // 回调在主线程中更新UI
            .map { users ->
                // 操作符：转换数据，例如过滤出活跃用户
                users.filter { it.isActive }
            }
            .doOnError { error ->
                // 可在此处记录日志或做其他处理
                Log.e("ApiError", "Error fetching users", error)
            }
            .subscribe(
                { activeUsers ->
                    // 请求成功回调，更新UI
                    // 例如显示用户列表
                    Log.d("ApiSuccess", "Active users count: ${activeUsers.size}")
                },
                { error ->
                    // 错误处理，例如显示错误信息
                    Log.e("ApiError", "Request failed", error)
                }
            )*/

        // 为了能够随时取消请求，保持返回的Disposable引用是必要的

        /*val disposable1: Disposable = apiService.getUsers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { users ->
                // 将List<User>拆分成一个个User对象
                Observable.fromIterable(users)
            }
            .filter { user ->
                // 过滤出需要的数据，比如只保留名字不为空的用户
                user.name.isNotEmpty()
            }
            .toList() // 收集回List<User>，注意这会返回Single<List<User>>
            .subscribe(

                { filteredUsers ->
                    // 成功回调
                    Log.d("FilterResult", "Filtered user count: ${filteredUsers.size}")
                },
                { error ->
                    // 错误处理
                    Log.e("FilterError", "Error in filter pipeline", error)
                }
            )*/

        // 当需要取消订阅时
        //disposable1.dispose()


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //不要重复创建多个viewmodel
        val viewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.button32.setOnClickListener {
            it
            viewModel.loadUsers(apiService)
        }

        binding.button41.setOnClickListener {
            it
            getData()
        }

        binding.button42.setOnClickListener {
            it
            getDataOnLocal()
        }


        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        viewModel.usersLiveData.observe(viewLifecycleOwner, Observer { usersLiveData ->
            // 更新UI，比如刷新列表
            binding.textviewFirst.text = usersLiveData.iterator().next().name
            /*usersLiveData.iterator().next()
                .let { return@let it.id.toString() + ":" + it.name + it.isActive }*/
            Log.d("UserActivity", "Active users count: ${usersLiveData.size}")
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCompositeDisposable.clear();
        _binding = null
    }
}