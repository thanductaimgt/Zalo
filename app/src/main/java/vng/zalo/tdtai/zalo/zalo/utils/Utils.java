package vng.zalo.tdtai.zalo.zalo.utils;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static Map<String, Object> RoomItemDocumentToMap(DocumentSnapshot doc){
        Map<String, Object> res = new HashMap<>();

        res.put("avatar",doc.get("avatar"));
        res.put("name",doc.get("name"));
        res.put("unseenMsgNum",doc.get("unseenMsgNum"));

        return res;
    }
}