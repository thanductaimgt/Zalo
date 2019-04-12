package vng.zalo.tdtai.zalo.zalo.models;

import java.util.Date;

import vng.zalo.tdtai.zalo.zalo.ZaloApplication;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.DEFAULT_AVATAR;

public class MessageModel extends DataModel {
    public Long id;
    public String senderPhone = ZaloApplication.sCurrentUserPhone;
    public String content = "default content";
    public Date createdTime = new Date();
    public String avatar = DEFAULT_AVATAR;

    public MessageModel(){}

    public MessageModel(Long id, String senderPhone, String content, Date createdTime, String avatar){
        this.id = id;
        this.senderPhone = senderPhone;
        this.content = content;
        this.createdTime = createdTime;
        this.avatar = avatar;
    }
}