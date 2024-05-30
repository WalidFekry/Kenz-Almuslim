package com.benkkstudio.bsjson.Interface;

import com.androidnetworking.error.ANError;

import org.json.JSONObject;

public interface BSJsonV2Listener {
    void onLoaded(String response);
    void onError(String error);
}
