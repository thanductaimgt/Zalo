package vng.zalo.tdtai.zalo.zalo.database.model.room;

import vng.zalo.tdtai.zalo.zalo.utils.Model;

public class MessageModel extends Model {
    public int msgId;
    public int senderId;
    public String message;
    public long timeStamp;
    public String avatarLink;

    public MessageModel(int msgId, int senderId, String message, long timeStamp, String avatarLink){
        this.msgId = msgId;
        this.senderId = senderId;
        this.message = message;
        this.timeStamp = timeStamp;
        this.avatarLink = avatarLink;
    }
}