package vng.zalo.tdtai.zalo.ui.chat.emoji

import androidx.fragment.app.FragmentManager
import dagger.BindsInstance
import dagger.Subcomponent
import dagger.android.AndroidInjector
import vng.zalo.tdtai.zalo.ui.chat.emoji.sticker.StickerSetComponent

@Subcomponent(modules = [EmojiModule::class])
interface EmojiComponent: AndroidInjector<EmojiFragment> {
    @Subcomponent.Factory
    interface Factory{
        fun create(
                @BindsInstance fragmentManager: FragmentManager
        ):AndroidInjector<EmojiFragment>
    }

    fun stickerSetComponentFactory():StickerSetComponent.Factory
}