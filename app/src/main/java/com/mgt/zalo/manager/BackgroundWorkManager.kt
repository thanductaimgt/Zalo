package com.mgt.zalo.manager

import android.util.Log
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.addTo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundWorkManager @Inject constructor() {
    private val defaultOnError = { t: Throwable ->
        Log.e(TAG, "Error from Zalo")
        t.printStackTrace()
    }

    fun <T> single(callable: () -> T, observer: SingleObserver<in T>) {
        Single.fromCallable {
            callable()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    fun <T> single(callable: () -> T, compositeDisposable: CompositeDisposable? = null, onSuccess: (result: T) -> Unit) {
        Single.fromCallable {
            callable()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onSuccess(it)
                }, defaultOnError)
                .addTo(compositeDisposable)
    }

    fun <T> observable(callable: (emitter: ObservableEmitter<T>) -> Any, observer: Observer<in T>) {
        Observable.create<T> { emitter ->
            callable(emitter)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    fun completable(callable: () -> Any, observer: CompletableObserver) {
        Completable.fromCallable {
            callable()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    fun completable(callable: () -> Any, compositeDisposable: CompositeDisposable? = null, onComplete: (() -> Unit)? = null) {
        Completable.fromCallable {
            callable()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onComplete?.invoke()
                }, defaultOnError)
                .addTo(compositeDisposable)
    }
}