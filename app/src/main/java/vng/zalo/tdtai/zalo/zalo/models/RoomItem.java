package vng.zalo.tdtai.zalo.zalo.models;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class RoomItem extends DataModel {
    public String roomId;
    public String avatar;
    public String lastMsg;
    public Timestamp lastMsgTime;
    public String name;
    public int unseenMsgNum;
    public int roomType;

    //mapping firestore data to POJO needs empty constructor
    public RoomItem(){}

    public final Map<String, Object> toMap(){
        Map<String, Object> res = new HashMap<>();

        res.put("avatar",this.avatar);
        res.put("lastMsg",this.lastMsg);
        res.put("lastMsgTime",this.lastMsgTime);
        res.put("name",this.name);
        res.put("unseenMsgNum",this.unseenMsgNum);
        res.put("roomType",this.roomType);

        return res;
    }

    @NonNull
    @Override
    public String toString() {
        return this.toMap().toString();
    }
}