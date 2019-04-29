package vng.zalo.tdtai.zalo.zalo.models;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

import vng.zalo.tdtai.zalo.zalo.utils.Constants;

public class Message extends DataModel {
    public String content;
    public Timestamp createdTime;
    public String senderPhone;
    public String senderAvatar;
    public int type;

    public Message(){}

    public final Map<String, Object> toMap(){
        Map<String, Object> res = new HashMap<>();

        res.put("content",content);
        res.put("createdTime",createdTime);
        res.put("senderPhone",senderPhone);
        res.put("type", type);

        return res;
    }
}