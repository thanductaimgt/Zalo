package vng.zalo.tdtai.zalo.zalo.models;

public class MessageModel extends DataModel {
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