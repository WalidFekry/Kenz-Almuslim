package com.kenz.almuslim.ui.splash;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.benkkstudio.bsjson.BSJson;
import com.benkkstudio.bsjson.BSObject;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.kenz.almuslim.BuildConfig;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.ui.main.MainActivity;
import com.kenz.almuslim.utils.CommonUtils;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(this::checkPermissions, 1000);
    }

    private void checkPermissions() {
        if (!hasStoragePermissions()) {
            showPermissionExplanationDialog();
        } else {
            loadAppSettings();
        }
    }

    private boolean hasStoragePermissions() {
        return checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissionExplanationDialog() {
        DialogWhy dialogWhy = DialogWhy.getInstance();
        dialogWhy.setListener(() -> {
            dialogWhy.dismiss();
            requestPermissions();
        });
        dialogWhy.show(getSupportFragmentManager(), DialogWhy.TAG);
    }

    private void requestPermissions() {
        handler.postDelayed(() -> TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        loadAppSettings();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        loadAppSettings();
                    }
                })
                .setPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.SET_WALLPAPER,
                        Manifest.permission.INTERNET
                )
                .check(), 500);
    }

    private void loadAppSettings() {
            launchMainActivity();
    }


    private void launchMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
