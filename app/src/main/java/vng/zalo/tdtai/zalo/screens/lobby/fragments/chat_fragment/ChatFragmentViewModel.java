package vng.zalo.tdtai.zalo.screens.lobby.fragments.chat_fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

class ChatFragmentViewModel extends ViewModel {
    MutableLiveData<List<ChatItemModel>> chatDataItemList;

    ChatFragmentViewModel(){
        //test
        chatDataItemList = new MutableLiveData<>();
        List<ChatItemModel> dataItemList = new ArrayList<>();
        Date date = new Date(123456789);

        dataItemList.add(new ChatItemModel(1,"Zalo careers","chuc mung ban da tro thanh nhan vien chinh thuc cua zalo !",date,true,"http://cdn.onlinewebfonts.com/svg/img_311846.png"));
        dataItemList.add(new ChatItemModel(1992,"Ms. Hoa","Ok Tai nha",date,false,"https://lh3.googleusercontent.com/h4ctnfcavolkVSouRjmgfeyz4Ch_706IL8GO1iqK7JVh9rqd7iIqDlmzMsSd6yNR8w"));
        chatDataItemList.setValue(dataItemList);
        //
    }
}