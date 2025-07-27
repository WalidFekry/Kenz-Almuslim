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
        if (!CommonUtils.isNetworkConnected(this)) {
            launchMainActivity();
            return;
        }

        BSObject request = new BSObject();
        request.addProperty("method_name", "settings");

        new BSJson.Builder(this)
                .setServer(Constant.SERVER_URL)
                .setObject(request.getProperty())
                .setMethod(BSJson.METHOD_POST)
                .setListener(new BSJsonV2Listener() {
                    @Override
                    public void onLoaded(String response) {
                        parseSettingsResponse(response);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(SplashActivity.this, R.string.network_error_try_later, Toast.LENGTH_SHORT).show();
                    }
                }).load();
    }

    private void parseSettingsResponse(String response) {
        try {
            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            JsonArray data = root.getAsJsonArray(Constant.TAG_ROOT);
            JsonObject settings = data.get(0).getAsJsonObject();

            String appVersion = settings.get("application_id").getAsString();
            if (!appVersion.equals(BuildConfig.VERSION_NAME)) {
                String updateUrl = settings.get("update_url").getAsString();
                String updateMessage = settings.get("update_message").getAsString();
                showUpdateDialog(updateUrl, updateMessage);
            } else {
                Constant.privacy_police = settings.get("privacy_police").getAsString();
                launchMainActivity();
            }

        } catch (JsonSyntaxException | IllegalStateException e) {
            Log.e("SplashActivity", "Error parsing settings", e);
            Toast.makeText(this, R.string.network_error_try_later, Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateDialog(String updateUrl, String updateMessage) {
        Dialog dialog = new Dialog(this, R.style.FullDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_update, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
        }

        TextView messageView = view.findViewById(R.id.update_message);
        messageView.setText(updateMessage);

        view.findViewById(R.id.allow_button).setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl)));
        });

        dialog.show();
    }

    private void launchMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
