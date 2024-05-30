package com.kenz.almuslim.data.downloadservice;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.kenz.almuslim.data.utils.Logger;


public class DownloadFile extends AsyncTask<String, String, String> {
    private String downloadDirectory, downloadUrl;
    private DownloadListener downloadListener;
    private String filename;

    public DownloadFile(String downloadDirectory, String downloadUrl, DownloadListener downloadListener) {
        this.downloadDirectory = downloadDirectory;
        this.downloadUrl = downloadUrl;
        this.downloadListener = downloadListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        downloadListener.onStart();
    }


    @Override
    protected String doInBackground(String... sss) {
        int count;
        try {
            URL url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            int lengthOfFile = connection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            filename = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
            File directory = new File(downloadDirectory);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File checkFile = new File(downloadDirectory + filename);
            if (checkFile.exists()) {
                return "File already downloaded !";
            } else {
                OutputStream output = new FileOutputStream(downloadDirectory + filename);
                byte[] data = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
                return "Downloaded at: " + downloadDirectory + filename;
            }
        } catch (Exception e) {
            Logger.log(e.getMessage());
        }
        return "Something went wrong";
    }

    protected void onProgressUpdate(String... progress) {
        downloadListener.onProgress(Integer.parseInt(progress[0]));
    }

    @Override
    protected void onPostExecute(String message) {
        downloadListener.onFinish(message, downloadDirectory, filename);
    }
}
