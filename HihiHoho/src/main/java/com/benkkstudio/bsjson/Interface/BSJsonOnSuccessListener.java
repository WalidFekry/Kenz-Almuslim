package com.benkkstudio.bsjson.Interface;

public interface BSJsonOnSuccessListener {
    void onSuccess(int statusCode, byte[] responseBody);
    void onFiled(int statusCode, byte[] responseBody, Throwable error);
}
