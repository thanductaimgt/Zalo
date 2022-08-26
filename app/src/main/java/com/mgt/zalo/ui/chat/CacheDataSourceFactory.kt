package com.mgt.zalo.ui.chat

import android.content.Context
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

class CacheDataSourceFactory(private val context: Context, private val maxCacheSize: Long, private val maxFileSize: Long) : DataSource.Factory {
    private val defaultDataSourceFactory: DefaultDataSource.Factory =
        DefaultDataSource.Factory(this.context)

    @Suppress("DEPRECATION")
    override fun createDataSource(): DataSource {
        if (dataSource == null) {
            synchronized(dataSourceLock) {
                if (dataSource == null) {
                    val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
                    val simpleCache = SimpleCache(File(context.cacheDir, "media"), evictor)
                    dataSource = CacheDataSource(simpleCache, defaultDataSourceFactory.createDataSource(),
                            FileDataSource(), CacheDataSink(simpleCache, maxFileSize),
                            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null)
                }
            }
        }
        return dataSource!!
    }

    companion object {
        private var dataSource: DataSource? = null
        private var dataSourceLock = object {}
    }
}