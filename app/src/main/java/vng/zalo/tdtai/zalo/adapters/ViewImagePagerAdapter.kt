package vng.zalo.tdtai.zalo.adapters

import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import vng.zalo.tdtai.zalo.abstracts.MovableFragmentStatePagerAdapter
import vng.zalo.tdtai.zalo.models.message.ImageMessage
import vng.zalo.tdtai.zalo.views.fragments.ViewImageFragment

class ViewImagePagerAdapter(private val clickListener: View.OnClickListener, fm: FragmentManager) : MovableFragmentStatePagerAdapter(fm/*, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT*/) {
    var imageMessages = ArrayList<ImageMessage>()

    override fun getItem(position: Int): Fragment {
        return ViewImageFragment(clickListener, imageMessages[position])
    }

    override fun getItemId(position: Int): String {
        return imageMessages[position].id!!
    }

    override fun getCount(): Int {
        return imageMessages.size
    }

    override fun getItemPosition(any: Any): Int {
        val fragment = any as ViewImageFragment
        return imageMessages.indexOfFirst { it.id == fragment.imageMessage.id }.let {
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