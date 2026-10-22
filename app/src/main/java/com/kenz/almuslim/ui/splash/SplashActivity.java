package com.kenz.almuslim.ui.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.kenz.almuslim.R;
import com.kenz.almuslim.ui.main.MainActivity;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable checkPermissionsRunnable = this::checkPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install AndroidX SplashScreen API for Android 12+ (API 31+) compatibility
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(checkPermissionsRunnable, 1000);
    }

    private void checkPermissions() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (!hasStoragePermissions()) {
            showPermissionExplanationDialog();
        } else {
            loadAppSettings();
        }
    }

    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+, including Android 13, 14, 15+)
            // Scoped storage handles file operations without needing runtime permissions on startup
            return new String[0];
        } else {
            // Android 6 - 9 (API 23 - 28)
            return new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }

    private boolean hasStoragePermissions() {
        String[] permissions = getRequiredPermissions();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showPermissionExplanationDialog() {
        if (isFinishing() || isDestroyed() || getSupportFragmentManager().isStateSaved()) {
            loadAppSettings();
            return;
        }

        DialogWhy dialogWhy = DialogWhy.getInstance();
        dialogWhy.setListener(() -> {
            if (!isFinishing() && !isDestroyed()) {
                try {
                    dialogWhy.dismiss();
                } catch (Exception ignored) {
                }
                requestPermissions();
            }
        });

        try {
            dialogWhy.show(getSupportFragmentManager(), DialogWhy.TAG);
        } catch (Exception e) {
            loadAppSettings();
        }
    }

    private void requestPermissions() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        String[] permissions = getRequiredPermissions();
        if (permissions.length == 0) {
            loadAppSettings();
            return;
        }

        TedPermission.create()
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
                .setPermissions(permissions)
                .check();
    }

    private void loadAppSettings() {
        launchMainActivity();
    }

    private void launchMainActivity() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
