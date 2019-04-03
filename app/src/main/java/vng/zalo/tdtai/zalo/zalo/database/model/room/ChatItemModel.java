package vng.zalo.tdtai.zalo.zalo.database.model.room;

import java.util.Date;

import androidx.room.Entity;
import vng.zalo.tdtai.zalo.zalo.utils.Model;

@Entity(tableName = "ChatItem")
public class ChatItemModel extends Model {
    public int id;
    public String name;
    public String description;
    public Date date;
    public boolean showIcon;
    public String avatar;

    public ChatItemModel(int id, String name, String description, Date date, boolean showIcon, String avatar){
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.showIcon = showIcon;
        this.avatar = avatar;
    }
}