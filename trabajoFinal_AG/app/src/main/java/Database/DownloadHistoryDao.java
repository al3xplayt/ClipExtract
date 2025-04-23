package Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DownloadHistoryDao {

    @Insert
    void insert(DownloadHistory downloadHistory);
    @Query("DELETE FROM download_history")
    void deleteAllHistory();
    @Query("SELECT * FROM download_history")
    List<DownloadHistory> getAllHistory();

    @Delete
    void delete(DownloadHistory downloadHistory);


}

