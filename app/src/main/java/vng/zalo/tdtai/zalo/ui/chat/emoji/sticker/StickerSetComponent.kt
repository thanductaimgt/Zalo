package vng.zalo.tdtai.zalo.ui.chat.emoji.sticker

import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [StickerSetModule::class])
interface StickerSetComponent {
    @Subcomponent.Factory
    interface Factory{
        fun create(@BindsInstance bucketName:String):StickerSetComponent
    }
}