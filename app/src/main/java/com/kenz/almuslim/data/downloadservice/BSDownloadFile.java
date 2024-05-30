package com.kenz.almuslim.data.downloadservice;

import android.app.Activity;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;

import java.io.File;

import com.kenz.almuslim.data.widgets.ProgressLoader;

public class BSDownloadFile {
    private Activity activity;
    private String url;
    private String downloadDirectory;
    private ProgressLoader progressLoader;
    private BSDownloadListener bsDownloadListener;
    private BSDownloadFile(Activity activity, String url, String downloadDirectory, BSDownloadListener bsDownloadListener) {
        this.activity = activity;
        this.url = url;
        this.downloadDirectory = downloadDirectory;
        this.bsDownloadListener = bsDownloadListener;
        progressLoader = new ProgressLoader(activity);
        downloadNow();
    }

    private void downloadNow(){
        progressLoader.show();
        File directory = new File(downloadDirectory);
        if (!directory.exists()){
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        PRDownloader.download(url, downloadDirectory, url.substring(url.lastIndexOf('/') + 1))
                .build()
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        progressLoader.dismiss();
                        bsDownloadListener.onFinish(downloadDirectory, url.substring(url.lastIndexOf('/') + 1));
                    }

                    @Override
                    public void onError(Error error) {
                        Toast.makeText(activity, error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                        progressLoader.dismiss();
                    }
                });
    }

    public static class Builder {
        private Activity activity;
        private String url;
        private String downloadDirectory;
        private BSDownloadListener bsDownloadListener;
        public Builder(Activity activity) {
            this.activity = activity;
        }

        public BSDownloadFile.Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public BSDownloadFile.Builder setDirectory(String downloadDirectory) {
            this.downloadDirectory = downloadDirectory;
            return this;
        }

        public BSDownloadFile.Builder setListener(BSDownloadListener bsDownloadListener) {
            this.bsDownloadListener = bsDownloadListener;
            return this;
        }


        public BSDownloadFile download() {
            return new BSDownloadFile(activity, url, downloadDirectory, bsDownloadListener);
        }
    }
}
