package vng.zalo.tdtai.zalo.ui.chat.view_image

import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.upstream.DataSource
import vng.zalo.tdtai.zalo.abstracts.MovableFragmentStatePagerAdapter
import vng.zalo.tdtai.zalo.model.message.ResourceMessage
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Inject
import javax.inject.Named

class ViewMediaAdapter @Inject constructor(
        @Named(Constants.ACTIVITY_NAME) private val clickListener: View.OnClickListener,
        @Named(Constants.ACTIVITY_NAME) fragmentManager: FragmentManager,
        val exoPlayer:ExoPlayer,
        private val cacheDataSourceFactory:DataSource.Factory,
        private val extractorsFactory:ExtractorsFactory
) : MovableFragmentStatePagerAdapter(fragmentManager/*, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT*/) {
    var resourceMessages = ArrayList<ResourceMessage>()

    override fun getItem(position: Int): Fragment {
        return ViewImageFragment(clickListener, resourceMessages[position], exoPlayer, cacheDataSourceFactory, extractorsFactory)
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