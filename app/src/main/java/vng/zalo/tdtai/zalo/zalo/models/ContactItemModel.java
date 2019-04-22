package vng.zalo.tdtai.zalo.zalo.models;

import java.util.Date;

import vng.zalo.tdtai.zalo.zalo.ZaloApplication;

import static vng.zalo.tdtai.zalo.zalo.utils.Constants.DEFAULT_AVATAR;

public class ContactItemModel extends DataModel {
    public String avatar;
    public String phone;

    public ContactItemModel(){}

    public ContactItemModel(String phone, String avatar){
        this.phone = phone;
        this.avatar = avatar;
    }
}