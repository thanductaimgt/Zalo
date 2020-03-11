package vng.zalo.tdtai.zalo.ui.chat.emoji

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import vng.zalo.tdtai.zalo.model.StickerSetItem
import vng.zalo.tdtai.zalo.ui.chat.emoji.sticker.StickerSetFragment
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Inject
import javax.inject.Named

class EmojiAdapter @Inject constructor(
        @Named(Constants.FRAGMENT_NAME) fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var stickerSetItems = ArrayList<StickerSetItem>()

    override fun getItem(position: Int): Fragment {
        return StickerSetFragment(stickerSetItems[position].bucketName!!)
    }

    override fun getCount(): Int {
        return stickerSetItems.size
    }
}