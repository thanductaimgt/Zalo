package vng.zalo.tdtai.zalo.zalo.database.model.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ChatItemModel.class},version = 1,exportSchema = false)
public abstract class ZaloDatabase extends RoomDatabase {
    abstract ChatItemDao getChatItemDao();

    public static ZaloDatabase getInstance(Context context){
        return ZaloDatabaseHelper.getInstance(context);
    }

    private static class ZaloDatabaseHelper{
        private static ZaloDatabase getInstance(Context context){
            return Room.databaseBuilder(context.getApplicationContext(),ZaloDatabase.class,"ZaloDB")
                    .allowMainThreadQueries()
                    .build();
        }
    }
    //TODO: change DB
}