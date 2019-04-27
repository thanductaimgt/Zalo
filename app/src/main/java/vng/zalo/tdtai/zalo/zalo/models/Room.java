package vng.zalo.tdtai.zalo.zalo.models;

import com.google.firebase.Timestamp;

import java.util.Map;

public class Room {
    public String id;
    public String avatar;
    public Timestamp createdTime;
    public String name;

    public Map<String, RoomMember> memberMap;
}