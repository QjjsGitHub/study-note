package com.example.study.internet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.study.internet.FirstFragment.User
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class UserViewModel : ViewModel() {

    // 用于向UI公开数据的LiveData
    val usersLiveData = MutableLiveData<List<User>>()

    private val compositeDisposable = CompositeDisposable()

    public fun loadUsers(apiService: FirstFragment.ApiService) {
        // 发起网络请求，通过RxJava获取数据并更新LiveData
        val disposable = getLocalUser()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                object : Consumer<List<User>> {
                    override fun accept(users: List<User>) {

                        usersLiveData.value = users

                        var string = ""

                        var iterator = users.iterator()

                        while (iterator.hasNext()) {
                            val s = iterator.next()
                                .let { it.id.toString() + it.name + it.isActive }
                            string += s

                        }
                        Log.d(InternetActivity.INTERNET_ACTIVITY_TAG, "on next: " + string)
                    }
                },
                object : Consumer<Throwable> {

                    override fun accept(p0: Throwable?) {
                        Log.d(InternetActivity.INTERNET_ACTIVITY_TAG, "on Error: " + p0?.message)

                    }

                }
            )
        compositeDisposable.add(disposable)
    }

    fun getLocalUser(): Observable<List<User>> {

        val list = ArrayList<User>(10)

        list.add(User(10, "one1"))
        list.add(User(11, "one2"))
        list.add(User(12, "one3"))
        list.add(User(13, "one4"))
        list.add(User(14, "one5"))

        return Observable.create<List<User>>(ObservableOnSubscribe<List<User>> { emitter ->
            emitter.onNext(list) // 发送值
            emitter.onComplete() // 完成发射
        })
    }

    override fun onCleared() {
        // ViewModel销毁时取消所有订阅
        compositeDisposable.clear()
        super.onCleared()
    }
}