package com.mgt.zalo.util

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.mgt.zalo.manager.ResourceManager
import com.squareup.picasso.*
import com.squareup.picasso.Target
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoader @Inject constructor(private val resourceManager: ResourceManager){
    fun load(url:String?, imageView: ImageView, applyConfig: ((requestCreator: RequestCreator) -> Unit)? = null){
        Picasso.get().smartLoad(url, resourceManager, imageView, applyConfig)
    }

    fun load(url:String?, target: Target, applyConfig: ((requestCreator: RequestCreator) -> Unit)? = null){
        Picasso.get().smartLoad(url, resourceManager, target, applyConfig)
    }

    fun load(resId:Int, imageView: ImageView, applyConfig: ((requestCreator: RequestCreator) -> Unit)? = null){
        Picasso.get().load(resId)
                .also { applyConfig?.invoke(it) }
                .into(imageView)
    }
}

private fun Picasso.loadCompat(url: String?, resourceManager: ResourceManager): RequestCreator {
    val doCache: Boolean
    val urlToLoad: String?
    if (url == null || resourceManager.isNetworkUri(url) || resourceManager.isContentUri(url)) {
        urlToLoad = url
        doCache = url != null && resourceManager.isNetworkUri(url)
    } else {
        urlToLoad = "file://$url"
        doCache = false
    }
    return load(urlToLoad).let { if (!doCache) it.networkPolicy(NetworkPolicy.NO_STORE) else it }
}

fun Picasso.smartLoad(url: String?, resourceManager: ResourceManager, imageView: ImageView, applyConfig: ((requestCreator: RequestCreator) -> Unit)? = null) {
    var requestCreator = loadCompat(url, resourceManager)
    applyConfig?.invoke(requestCreator)

    requestCreator.networkPolicy(NetworkPolicy.OFFLINE)
            .into(imageView, object : Callback.EmptyCallback() {
                override fun onError(e: Exception?) {
                    requestCreator = Picasso.get().loadCompat(url, resourceManager)
                    applyConfig?.invoke(requestCreator)

                    requestCreator.networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(imageView)
                }
            })
}

fun Picasso.smartLoad(url: String?, resourceManager: ResourceManager, target: Target, applyConfig: ((requestCreator: RequestCreator) -> Unit)? = null) {
    var requestCreator = loadCompat(url, resourceManager)
    applyConfig?.invoke(requestCreator)

    requestCreator.networkPolicy(NetworkPolicy.OFFLINE)
            .into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    target.onPrepareLoad(placeHolderDrawable)
                }

                override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                    requestCreator = Picasso.get().loadCompat(url, resourceManager)
                    applyConfig?.invoke(requestCreator)

                    requestCreator.networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(object : Target {
                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                                override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
                                    target.onBitmapFailed(e, errorDrawable)
                                }

                                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                    target.onBitmapLoaded(bitmap, from)
                                }
                            })
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    target.onBitmapLoaded(bitmap, from)
                }
            })
}