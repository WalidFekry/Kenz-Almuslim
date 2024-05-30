package com.benkkstudio.bsjson;

import android.util.Base64;

public class Helpers {
    public static String toBase64(String input) {
        byte[] encodeValue = Base64.encode(input.getBytes(), Base64.DEFAULT);
        return new String(encodeValue);
    }

}
