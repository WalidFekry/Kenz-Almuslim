package com.benkkstudio.bsjson;

import com.loopj.android.http.AsyncHttpClient;

import java.io.Serializable;

public class Constant implements Serializable {
    public static AsyncHttpClient client;
    public static String purchaseCode;
    public static boolean enableLogging;
}
