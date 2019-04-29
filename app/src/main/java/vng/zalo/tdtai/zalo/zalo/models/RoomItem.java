package vng.zalo.tdtai.zalo.zalo.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class RoomItem extends DataModel {
    public String roomId;
    public String avatar;
    public String lastMsg;
    public Timestamp lastMsgTime;
    public String name;
    public Long unseenMsgNum;

    //mapping firestore data to POJO needs empty constructor
    public RoomItem(){}

    public final Map<String, Object> toMap(){
        Map<String, Object> res = new HashMap<>();

        res.put("avatar",this.avatar);
        res.put("lastMsg",this.lastMsg);
        res.put("lastMsgTime",this.lastMsgTime);
        res.put("name",this.name);
        res.put("unseenMsgNum",this.unseenMsgNum);

        return res;
    }

    @NonNull
    @Override
    public String toString() {
        return this.toMap().toString();
    }
}