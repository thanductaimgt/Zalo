package vng.zalo.tdtai.zalo.screens.lobby.fragments.chat_fragment;

import java.util.Date;
import vng.zalo.tdtai.zalo.screens.utils.Model;

public class ChatItemModel extends Model {
    public int id;
    public String name;
    public String description;
    public Date date;
    public boolean showIcon;
    public String avatar;

    ChatItemModel(int id, String name, String description, Date date, boolean showIcon, String avatar){
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.showIcon = showIcon;
        this.avatar = avatar;
    }
}