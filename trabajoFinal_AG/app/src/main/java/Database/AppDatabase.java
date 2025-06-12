package Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import Models.DownloadHistory;

@Database(entities = {DownloadHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DownloadHistoryDao downloadHistoryDao();
}

