package vng.zalo.tdtai.zalo.zalo.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vng.zalo.tdtai.zalo.zalo.ZaloApplication;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.DEFAULT_AVATAR;

public class Message extends DataModel {
    public String content;
    public Timestamp createdTime;
    public String senderPhone;
    public String senderAvatar;

    public Message(){}

    public static Message docToMessage(DocumentSnapshot doc, Map<String, RoomMember> memberMap){
        Message message = new Message();

        message.content = doc.getString("content");
        message.createdTime = doc.getTimestamp("createdTime");
        message.senderPhone = doc.getString("senderPhone");
        message.senderAvatar = memberMap.get(message.senderPhone).avatar;

        return message;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> res = new HashMap<>();

        res.put("content",content);
        res.put("createdTime",createdTime);
        res.put("senderPhone",senderPhone);

        return res;
    }
}