package vng.zalo.tdtai.zalo.factories

import android.content.Context
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import vng.zalo.tdtai.zalo.R
import java.io.File

class CacheDataSourceFactory(private val context: Context, private val maxCacheSize: Long, private val maxFileSize: Long) : DataSource.Factory {
    private val defaultDataSourceFactory: DefaultDataSourceFactory

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

    init {
        val userAgent: String = Util.getUserAgent(context, context.getString(R.string.label_app_name))
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        defaultDataSourceFactory = DefaultDataSourceFactory(this.context,
                bandwidthMeter,
                DefaultHttpDataSourceFactory(userAgent, bandwidthMeter))
    }

    companion object {
        private var dataSource: DataSource? = null
        private var dataSourceLock = object {}
    }
}