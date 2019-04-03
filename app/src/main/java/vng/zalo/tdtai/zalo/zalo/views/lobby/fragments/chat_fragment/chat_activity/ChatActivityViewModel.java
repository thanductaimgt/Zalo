package vng.zalo.tdtai.zalo.zalo.views.lobby.fragments.chat_fragment.chat_activity;

import java.util.ArrayList;
import java.util.List;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import vng.zalo.tdtai.zalo.zalo.database.model.room.MessageModel;

class ChatActivityViewModel extends ViewModel {
    MutableLiveData<List<MessageModel>> liveDataMsgList;

    ChatActivityViewModel(){
        List<MessageModel> messageModelList = new ArrayList<>();
        messageModelList.add(new MessageModel(0,1992,"Hi em",987654321,"http://cdn.onlinewebfonts.com/svg/img_311846.png"));
        messageModelList.add(new MessageModel(1,1992,"Chuc mung em",987654322,"http://cdn.onlinewebfonts.com/svg/img_311846.png"));
        messageModelList.add(new MessageModel(2,1998,"Thank you chi",987654323,"https://lh3.googleusercontent.com/h4ctnfcavolkVSouRjmgfeyz4Ch_706IL8GO1iqK7JVh9rqd7iIqDlmzMsSd6yNR8w"));
        messageModelList.add(new MessageModel(3,1998,"I know it",987654324,"https://lh3.googleusercontent.com/h4ctnfcavolkVSouRjmgfeyz4Ch_706IL8GO1iqK7JVh9rqd7iIqDlmzMsSd6yNR8w"));
        messageModelList.add(new MessageModel(4,1992,"ok",987654325,"http://cdn.onlinewebfonts.com/svg/img_311846.png"));

        liveDataMsgList = new MutableLiveData<>();
        liveDataMsgList.setValue(messageModelList);
    }
}