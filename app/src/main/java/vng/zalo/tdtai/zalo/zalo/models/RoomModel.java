package vng.zalo.tdtai.zalo.zalo.models;

import java.util.Date;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.DEFAULT_AVATAR;

public class RoomModel extends DataModel {
    public Long id;
    public String name = "default room name";
    public String lastMsgContent = "default last message content";
    public Date date = new Date();
    public boolean showIcon = false;
    public String avatar = DEFAULT_AVATAR;

    //mapping firestore data to POJO needs empty constructor
    public RoomModel(){}

    public RoomModel(Long id, String name, String lastMsgContent, Date date, boolean showIcon, String avatar){
        this.id = id;
        this.name = name;
        this.lastMsgContent = lastMsgContent;
        this.date = date;
        this.showIcon = showIcon;
        this.avatar = avatar;
    }
}