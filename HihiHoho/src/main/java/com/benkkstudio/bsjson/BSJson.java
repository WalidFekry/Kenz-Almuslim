package com.benkkstudio.bsjson;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.benkkstudio.bsjson.utils.BSShared;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class BSJson {
    public static final int METHOD_POST = 0;
    public static final int METHOD_GET = 1;
    private static final String IS_VERIFIED = "IS_VERIFIED";
    private final Context context;
    private final String server;
    private final JsonObject jsObj;
    private final BSJsonV2Listener bsJsonV2Listener;
    private final int method;

    private BSJson(Context context,
                   String server,
                   JsonObject jsObj,
                   BSJsonV2Listener bsJsonV2Listener,
                   int method) {
        this.context = context;
        this.server = server;
        this.jsObj = jsObj;
        this.bsJsonV2Listener = bsJsonV2Listener;
        this.method = method;
        if (BSShared.getSharedPref(context).contains(IS_VERIFIED)) {
            loadNow();
        } else {
            verifyNow();
        }
    }

    public static class initializing {
        public initializing() {
        }

        @NonNull
        public BSJson.initializing withSecret(String purchaseCode) {
            Constant.purchaseCode = purchaseCode;
            return this;
        }

        public BSJson.initializing enableLogging(boolean enableLogging) {
            Constant.enableLogging = enableLogging;
            return this;
        }
    }


    private void verifyNow() {
        if (Constant.enableLogging) {
            Log.d("BSJson : ", "Verify purchase to server");
        }
        AndroidNetworking.get("https://api.envato.com/v3/market/author/sale")
                .addHeaders("Authorization", "Bearer 031Cm94VBFWVIwOGuyvfTcvvmvF3EM9b")
                .addHeaders("User-Agent", "Purchase code verification on benkkstudio.xyz")
                .addQueryParameter("code", Constant.purchaseCode)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        loadNow();
                        BSShared.getSharedPref(context).write("IS_VERIFIED", true);
                    }

                    @Override
                    public void onError(ANError error) {
                        Toast.makeText(context, "Your purchase code not valid", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadNow() {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        if (jsObj != null) {
            AndroidNetworking.post(server + "?data=" + Helpers.toBase64(jsObj.toString()))
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsString(new StringRequestListener() {
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
                AndroidNetworking.post(server)
                        .setPriority(Priority.MEDIUM)
                        .setOkHttpClient(okHttpClient)
                        .build()
                        .getAsString(new StringRequestListener() {
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
                AndroidNetworking.get(server)
                        .setPriority(Priority.MEDIUM)
                        .setOkHttpClient(okHttpClient)
                        .build()
                        .getAsString(new StringRequestListener() {
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
        private Context context;
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
            return new BSJson(context, server, jsObj, bsJsonV2Listener, method);
        }
    }
}