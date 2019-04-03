package vng.zalo.tdtai.zalo.screens.lobby.fragments.chat_fragment.chat_activity;

import vng.zalo.tdtai.zalo.screens.utils.Model;

class MessageModel extends Model {
    int msgId;
    int senderId;
    String message;
    long timeStamp;
    String avatarLink;

    MessageModel(int msgId, int senderId, String message, long timeStamp, String avatarLink){
        this.msgId = msgId;
        this.senderId = senderId;
        this.message = message;
        this.timeStamp = timeStamp;
        this.avatarLink = avatarLink;
    }
}