package vng.zalo.tdtai.zalo.ui.chat.emoji.sticker

import android.view.View
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import vng.zalo.tdtai.zalo.di.ViewModelKey
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Named

@Module(includes = [StickerSetModule.ProvideModule::class])
interface StickerSetModule {
    @Binds
    @IntoMap
    @ViewModelKey(StickerSetViewModel::class)
    fun bindStickerSetViewModel(viewModel: StickerSetViewModel): ViewModel

    @Binds
    @Named(Constants.FRAGMENT_NAME)
    fun bindClickListener(stickerSetFragment: StickerSetFragment):View.OnClickListener

    @Module
    class ProvideModule{
        @Provides
        fun bucketName(stickerSetFragment: StickerSetFragment):String{
            return stickerSetFragment.bucketName
        }
    }
}