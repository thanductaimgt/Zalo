package vng.zalo.tdtai.zalo.storage.localdb

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface LocalDAO {
    @Insert
    fun insertVideoThumb()
}