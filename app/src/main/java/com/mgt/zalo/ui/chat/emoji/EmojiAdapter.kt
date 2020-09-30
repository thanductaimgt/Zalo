package com.mgt.zalo.ui.chat.emoji

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mgt.zalo.data_model.StickerSetItem
import com.mgt.zalo.ui.chat.emoji.sticker.StickerSetFragment
import javax.inject.Inject

class EmojiAdapter @Inject constructor(
        emojiFragment: EmojiFragment
) : FragmentStateAdapter(emojiFragment) {
    var stickerSetItems = ArrayList<StickerSetItem>()

    override fun createFragment(position: Int): Fragment {
        return StickerSetFragment(stickerSetItems[position].bucketName!!)
    }

    override fun getItemCount(): Int {
        return stickerSetItems.size
    }
}