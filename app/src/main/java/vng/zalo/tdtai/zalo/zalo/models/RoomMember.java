package vng.zalo.tdtai.zalo.zalo.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class RoomMember {
    public String avatar;
    public Timestamp joinDate;

    public static RoomMember docToRoomMember(DocumentSnapshot doc){
        RoomMember roomMember = new RoomMember();

        roomMember.avatar = doc.getString("avatar");
        roomMember.joinDate = doc.getTimestamp("joinDate");

        return roomMember;
    }
}