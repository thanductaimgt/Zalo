package vng.zalo.tdtai.zalo.zalo.database.model.room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ChatItemDao {
    @Insert
    void insertChatItem(ChatItemDao chatItemDao);

    @Delete
    void deleteChatItem(ChatItemDao chatItemDao);

    @Query("select * from ChatItem")
    List<ChatItemModel> selectAllChatItem();
}