//package vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity;
//
//import androidx.lifecycle.ViewModelProviders;
//import dagger.Module;
//import dagger.Provides;
//import vng.zalo.tdtai.zalo.zalo.viewmodels.ChatActivityViewModel;
//import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.chat_activity.ChatActivity;
//
//
//@Module
//public class ChatActivityModule {
//    private ChatActivity chatActivity;
//
//    public ChatActivityModule(ChatActivity chatActivity){
//        this.chatActivity = chatActivity;
//    }
//
//    @Provides
//    ChatActivityViewModel providesViewModel(){
//        return ViewModelProviders.of(chatActivity, new ChatActivityViewModelFactory(chatActivity.getIntent(), chatActivity.getApplication()))
//                .get(ChatActivityViewModel.class);
//    }
//}