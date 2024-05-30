package com.kenz.almuslim;

import android.app.Application;
import android.os.StrictMode;

import com.benkkstudio.bsjson.utils.BSShared;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.kenz.almuslim.data.local.LocalDataManager;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;

import io.reactivex.plugins.RxJavaPlugins;


public class App extends Application {


    public static LocalDataManager localDataManager;


    @Override
    public void onCreate() {
        super.onCreate();
        RxJavaPlugins.setErrorHandler(Throwable::printStackTrace);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        BSShared.getSharedPref(getApplicationContext()).write("IS_VERIFIED", true);
        FirebaseAnalytics.getInstance(this);
        localDataManager = new LocalDataManager(this);
    }
}
