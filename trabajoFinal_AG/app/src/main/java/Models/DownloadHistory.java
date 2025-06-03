package Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "download_history")
public class DownloadHistory {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String fileName;
    private String format;
    private String date;
    private String url;

    @Ignore
    public DownloadHistory(String fileName, String format, String date, String url) {
        this.fileName = fileName;
        this.format = format;
        this.date = date;
        this.url = url;
    }

    public DownloadHistory() {
        // Constructor sin argumentos que Room usará
    }

    // Getters y setters...
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
