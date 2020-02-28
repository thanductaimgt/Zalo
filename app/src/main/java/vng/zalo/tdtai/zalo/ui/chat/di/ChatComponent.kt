package vng.zalo.tdtai.zalo.ui.chat.di

import android.content.Intent
import dagger.BindsInstance
import dagger.Subcomponent
import dagger.android.AndroidInjector
import vng.zalo.tdtai.zalo.di.ActivityScope
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import vng.zalo.tdtai.zalo.ui.chat.emoji.EmojiComponent

@Subcomponent
interface ChatComponent:AndroidInjector<ChatActivity> {
    @Subcomponent.Factory
    interface Factory{
        fun create(
                @BindsInstance activity: ChatActivity,
                @BindsInstance intent: Intent): ChatComponent
    }

    fun emojiComponentFactory():EmojiComponent.Factory
}