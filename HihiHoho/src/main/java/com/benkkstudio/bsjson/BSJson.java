package com.benkkstudio.bsjson;


import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import okhttp3.OkHttpClient;

public class BSJson {
    public static final int METHOD_POST = 0;
    private final String server;
    private final JsonObject jsObj;
    private final BSJsonV2Listener bsJsonV2Listener;
    private final int method;

    private BSJson(String server, JsonObject jsObj, BSJsonV2Listener bsJsonV2Listener, int method) {
        this.server = server;
        this.jsObj = jsObj;
        this.bsJsonV2Listener = bsJsonV2Listener;
        this.method = method;
        loadNow();
    }


    private void loadNow() {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).build();

        if (jsObj != null) {
            AndroidNetworking.post(server + "?data=" + Helpers.toBase64(jsObj.toString())).setPriority(Priority.MEDIUM).build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    if (bsJsonV2Listener != null) {
                        bsJsonV2Listener.onLoaded(response);
                    }
                }

                @Override
                public void onError(ANError error) {
                    if (bsJsonV2Listener != null) {
                        bsJsonV2Listener.onError(error.getErrorBody());
                    }
                }
            });
        } else {
            if (method == METHOD_POST) {
                AndroidNetworking.post(server).setPriority(Priority.MEDIUM).setOkHttpClient(okHttpClient).build().getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (bsJsonV2Listener != null) {
                            bsJsonV2Listener.onLoaded(response);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        if (bsJsonV2Listener != null) {
                            bsJsonV2Listener.onError(error.getErrorBody());
                        }
                    }
                });
            } else {
                AndroidNetworking.get(server).setPriority(Priority.MEDIUM).setOkHttpClient(okHttpClient).build().getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        if (bsJsonV2Listener != null) {
                            bsJsonV2Listener.onLoaded(response);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        if (bsJsonV2Listener != null) {
                            bsJsonV2Listener.onError(error.getErrorBody());
                        }
                    }
                });
            }
        }
    }

    public static class Builder {
        private final Context context;
        private String server;
        private JsonObject jsObj;
        private BSJsonV2Listener bsJsonV2Listener;
        private int method;

        public Builder(Context context) {
            this.context = context;
        }

        @NonNull
        public BSJson.Builder setServer(String server) {
            this.server = server;
            return this;
        }

        @NonNull
        public BSJson.Builder setMethod(int method) {
            this.method = method;
            return this;
        }

        public BSJson.Builder setObject(JsonObject jsObj) {
            this.jsObj = jsObj;
            return this;
        }

        @NonNull
        public BSJson.Builder setListener(BSJsonV2Listener bsJsonV2Listener) {
            this.bsJsonV2Listener = bsJsonV2Listener;
            return this;
        }

        public BSJson load() {
            return new BSJson(server, jsObj, bsJsonV2Listener, method);
        }
    }
}