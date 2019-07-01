package vng.zalo.tdtai.zalo.zalo.models;

import com.google.firebase.Timestamp;

public class UserInfo {
    public String phone;
    public String avatar;
    public Timestamp birthDate;
    public boolean isMale;
    public Timestamp joinDate;

    public UserInfo(){}

    @Override
    public String toString() {
        return "{phone: "+phone+", avatar: "+avatar+", birthDate: "+birthDate+"}";
    }
}