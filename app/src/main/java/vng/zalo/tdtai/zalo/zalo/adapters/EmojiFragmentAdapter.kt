package vng.zalo.tdtai.zalo.zalo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vng.zalo.tdtai.zalo.zalo.models.StickerSet
import vng.zalo.tdtai.zalo.zalo.models.StickerSetItem
import vng.zalo.tdtai.zalo.zalo.views.fragments.StickerSetFragment

class EmojiFragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var stickerSetItems = ArrayList<StickerSetItem>()

    override fun getItem(position: Int): Fragment {
        return StickerSetFragment(stickerSetItems[position].bucketName!!)
    }

    override fun getCount(): Int {
        return stickerSetItems.size
    }
}