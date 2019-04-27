package vng.zalo.tdtai.zalo.zalo.models;

import java.util.Date;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.DEFAULT_AVATAR;

public class RoomModel extends DataModel {
    public String id;
    public String name = "default room name";
    public String lastMsgContent = "";
    public Date lastMgsDate;
    public boolean showIcon = false;
    public String avatar = DEFAULT_AVATAR;

    //mapping firestore data to POJO needs empty constructor
    public RoomModel(){}

    public RoomModel(String id, String name, String lastMsgContent, Date lastMgsDate, boolean showIcon, String avatar){
        this.id = id;
        this.name = name;
        this.lastMsgContent = lastMsgContent;
        this.lastMgsDate = lastMgsDate;
        this.showIcon = showIcon;
        this.avatar = avatar;
    }
}