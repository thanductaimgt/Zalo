package vng.zalo.tdtai.zalo.zalo.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public class RoomItem extends DataModel {
    public String id;
    public String avatar;
    public String lastMsg;
    public Timestamp lastMsgTime;
    public String name;
    public Long unseenMsgNum;

    //mapping firestore data to POJO needs empty constructor
    public RoomItem(){}

    public static RoomItem docToRoomItem(DocumentSnapshot doc){
        RoomItem roomItem = new RoomItem();

        roomItem.id = doc.getId();
        roomItem.avatar = doc.getString("avatar");
        roomItem.lastMsg = doc.getString("lastMsg");
        roomItem.lastMsgTime = doc.getTimestamp("lastMsgTime");
        roomItem.name = doc.getString("name");
        roomItem.unseenMsgNum = doc.getLong("unseenMsgNum");

        return roomItem;
    }
}