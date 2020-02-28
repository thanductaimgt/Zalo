package vng.zalo.tdtai.zalo.ui.chat.view_image

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import vng.zalo.tdtai.zalo.abstracts.MovableFragmentStatePagerAdapter
import vng.zalo.tdtai.zalo.model.message.ResourceMessage
import vng.zalo.tdtai.zalo.ui.chat.CacheDataSourceFactory
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import javax.inject.Inject

class ViewMediaAdapter @Inject constructor(
        private val chatActivity: ChatActivity
) : MovableFragmentStatePagerAdapter(chatActivity.supportFragmentManager/*, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT*/) {
    var resourceMessages = ArrayList<ResourceMessage>()

    private val cacheDataSourceFactory= CacheDataSourceFactory(chatActivity, 100 * 1024 * 1024, 10 * 1024 * 1024)
    private val extractorsFactory= DefaultExtractorsFactory()

    private val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter.Builder(chatActivity).build()
    private val trackSelector: TrackSelector = DefaultTrackSelector(chatActivity)

    val exoPlayer = SimpleExoPlayer.Builder(chatActivity)
            .setBandwidthMeter(bandwidthMeter)
            .setTrackSelector(trackSelector)
            .build()

    override fun getItem(position: Int): Fragment {
        return ViewImageFragment(chatActivity, resourceMessages[position], exoPlayer, cacheDataSourceFactory, extractorsFactory)
    }

    override fun getItemId(position: Int): String {
        return resourceMessages[position].id!!
    }

    override fun getCount(): Int {
        return resourceMessages.size
    }

    override fun getItemPosition(any: Any): Int {
        val fragment = any as ViewImageFragment
        return resourceMessages.indexOfFirst { it.id == fragment.resourceMessage.id }.let {
            if (it == -1) {
                POSITION_NONE
            } else {
                it
            }
        }
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
//        super.restoreState(state, loader)
    }
}