package com.kenz.almuslim.data.downloadservice;

public interface DownloadListener {
    void onStart();
    void onProgress(int progress);
    void onFinish(String message, String directory, String filename);
}
