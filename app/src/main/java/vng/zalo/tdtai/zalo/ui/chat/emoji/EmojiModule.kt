package vng.zalo.tdtai.zalo.ui.chat.emoji

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.ui.chat.emoji.sticker.StickerSetFragment
import vng.zalo.tdtai.zalo.ui.chat.emoji.sticker.StickerSetModule
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Named

@Module(includes = [EmojiModule.ProvideModule::class])
interface EmojiModule{
    @ContributesAndroidInjector(modules = [StickerSetModule::class])
    fun stickerSetFragment():StickerSetFragment

    @Binds
    @IntoMap
    @ViewModelKey(EmojiViewModel::class)
    fun bindEmojiViewModel(emojiViewModel: EmojiViewModel): ViewModel

    @Module
    class ProvideModule{
        @Provides
        @Named(Constants.FRAGMENT_NAME)
        fun provideFragmentManager(emojiFragment: EmojiFragment):FragmentManager{
            return emojiFragment.childFragmentManager
        }
    }
}