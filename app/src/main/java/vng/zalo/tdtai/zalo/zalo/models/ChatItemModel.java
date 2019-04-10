package vng.zalo.tdtai.zalo.zalo.models;

import java.util.Date;

public class ChatItemModel extends DataModel {
    public int id;
    public String name;
    public String description;
    public Date date;
    public boolean showIcon;
    public String avatar;

    //mapping firestore data to POJO needs empty constructor
    public ChatItemModel(){}

    public ChatItemModel(int id, String name, String description, Date date, boolean showIcon, String avatar){
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.showIcon = showIcon;
        this.avatar = avatar;
    }
}