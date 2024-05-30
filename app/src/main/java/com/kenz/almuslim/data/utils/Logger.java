package com.kenz.almuslim.data.utils;

import android.net.Uri;
import android.util.Log;

public class Logger {
    public static void log(String string){
        Log.wtf("ABENK :", string);
    }

    public static void log(int string){
        Log.wtf("ABENK :", String.valueOf(string));
    }

    public static void log(boolean string){
        Log.wtf("ABENK :", String.valueOf(string));
    }

    public static void log(long string){
        Log.wtf("ABENK :", String.valueOf(string));
    }

    public static void log(byte[] string){
        Log.wtf("ABENK :", String.valueOf(string));
    }
    public static void log(float string){
        Log.wtf("ABENK :", String.valueOf(string));
    }
    public static void log(Uri string){
        Log.wtf("ABENK :", String.valueOf(string));
    }
}
