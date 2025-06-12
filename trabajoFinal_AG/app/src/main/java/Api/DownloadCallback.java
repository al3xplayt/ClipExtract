package Api;

import java.io.File;

public interface DownloadCallback {
    void onDownloadSuccess(File file);
    void onDownloadFailed(Exception e);
}

