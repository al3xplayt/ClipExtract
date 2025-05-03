package Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DownloadHistory.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DownloadHistoryDao downloadHistoryDao();
}

