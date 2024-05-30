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

import com.benkkstudio.bsjson.BSJson;
import com.benkkstudio.bsjson.BSObject;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.kenz.almuslim.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

import com.kenz.almuslim.BuildConfig;

import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.NativeLoaderListener;
import com.kenz.almuslim.ui.main.MainActivity;
import com.kenz.almuslim.utils.CommonUtils;

public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(() -> {
            String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            String secondPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            int checkVal = checkCallingOrSelfPermission(permission);
            int secondCheck = checkCallingOrSelfPermission(secondPermission);
            if (checkVal == PackageManager.PERMISSION_DENIED || secondCheck == PackageManager.PERMISSION_DENIED) {
                DialogWhy dialogWhy = DialogWhy.getInstance();
                dialogWhy.setListener(() -> {
                    dialogWhy.dismiss();
                    handler.postDelayed(() -> TedPermission.with(SplashActivity.this)
                                    .setPermissionListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted() {
                                            loadAbout();
                                        }

                                        @Override
                                        public void onPermissionDenied(List<String> deniedPermissions) {
                                            loadAbout();
                                        }
                                    })
                                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.ACCESS_NETWORK_STATE,
                                            Manifest.permission.SET_WALLPAPER,
                                            Manifest.permission.INTERNET)
                                    .check(),
                            500);
                });
                dialogWhy.show(getSupportFragmentManager(), DialogWhy.TAG);
            } else {
                loadAbout();
            }
        }, 1000);
    }

    private void loadNative() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
    }

    private void loadAbout() {
        if (!CommonUtils.isNetworkConnected(this)) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            return;
        }
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "settings");
        new BSJson.Builder(this)
                .setServer(Constant.SERVER_URL)
                .setObject(bsObject.getProperty())
                .setMethod(BSJson.METHOD_POST)
                .setListener(new BSJsonV2Listener() {
                    @Override
                    public void onLoaded(String response) {
                        try {
                            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                            JsonArray jsonArray = jsonObject.getAsJsonArray(Constant.TAG_ROOT);
                            JsonObject objJson = jsonArray.get(0).getAsJsonObject();
                            if (!objJson.get("application_id").getAsString().equals(BuildConfig.VERSION_NAME)) {
                                showUpdate(objJson.get("update_url").getAsString(), objJson.get("update_message").getAsString());
                            } else {
//                                Constant.banner_id = objJson.get("banner_id").getAsString();
//                                Constant.interstitial_id = objJson.get("interstital_id").getAsString();
//                                Constant.interstitial_click = Integer.parseInt(objJson.get("interstital_click").getAsString());
//                                Constant.native_id = objJson.get("native_id").getAsString();
//                                Constant.reward_id = objJson.get("reward_id").getAsString();
//                                Constant.facebook_banner_id = objJson.get("facebook_banner_id").getAsString();
//                                Constant.facebook_interstitial_id = objJson.get("facebook_interstitial_id").getAsString();
//                                Constant.facebook_native_id = objJson.get("facebook_native_id").getAsString();
//                                Constant.facebook_reward_id = objJson.get("facebook_reward_id").getAsString();
//                                Constant.AdsOptions.IDENTIFIER = objJson.get("ads_provider").getAsString();
//                                if (objJson.get("ads_provider").getAsString().equals(Constant.AdsOptions.DISABLE)) {
//                                    Constant.banner_id = "";
//                                    Constant.interstitial_id = "";
//                                    Constant.interstitial_click = 99999;
//                                    Constant.native_id = "";
//                                    Constant.facebook_banner_id = "";
//                                    Constant.facebook_interstitial_id = "";
//                                    Constant.facebook_native_id = "";
//                                }
//                                if (objJson.get("ads_provider").getAsString().equals(Constant.AdsOptions.FACEBOOK)) {
//                                    if (BuildConfig.DEBUG) {
//                                        AdSettings.setTestMode(true);
//                                    }
//                                    FacebookNativeLoader.load(SplashActivity.this, new NativeLoaderListener() {
//                                        @Override
//                                        public void onFinish() {
//                                            loadNative();
//                                        }
//                                    });
                                Constant.privacy_police = objJson.get("privacy_police").getAsString();
                                loadNative();

                            }
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                            Log.d("TAG : ", Objects.requireNonNull(e.getMessage()));
                            Toast.makeText(SplashActivity.this, getString(R.string.network_error_try_later), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onError(String error) {
                        //Log.d("TAG : ", error);
                        Toast.makeText(SplashActivity.this, getString(R.string.network_error_try_later), Toast.LENGTH_SHORT).show();
                    }
                }).load();
    }


    private void showUpdate(final String update_url, final String update_message) {
        final Dialog dialog = new Dialog(this, R.style.FullDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_update, null);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);
        TextView updatemessage = view.findViewById(R.id.update_message);
        updatemessage.setText(update_message);
        view.findViewById(R.id.allow_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(update_url)));
            }
        });
        dialog.show();
    }
}
