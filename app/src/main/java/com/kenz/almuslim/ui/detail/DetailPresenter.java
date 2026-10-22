package com.kenz.almuslim.ui.detail;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.benkkstudio.bsjson.BSJson;
import com.benkkstudio.bsjson.BSObject;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.benkkstudio.bsmob.BSMob;
import com.benkkstudio.bsmob.Interface.RewardListener;

import com.fangxu.allangleexpandablebutton.ButtonData;
import com.fangxu.allangleexpandablebutton.ButtonEventListener;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.adapter.AdapterWallpaper;
import com.kenz.almuslim.data.adapter.ImagePagerAdapter;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.downloadservice.BSDownloadFile;
import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.DatabaseHandler;
import com.kenz.almuslim.data.utils.SharedPref;
import com.kenz.almuslim.data.widgets.ProgressLoader;
import com.kenz.almuslim.databinding.ActivityDetailBinding;
import com.kenz.almuslim.ui.detail.done.DialogDone;
import com.kenz.almuslim.ui.detail.vertical.VerticalAdapter;
import com.kenz.almuslim.ui.zoom.ZoomActivity;
import com.kenz.almuslim.utils.CommonUtils;
import com.kenz.almuslim.utils.WallPaperCommonUtils;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class DetailPresenter extends BasePresenter<DetailView> {
    public static final String METHOD_DOWNLOAD = "DOWNLOAD";
    public static final String METHOD_SET_WALLPAPER = "SET_WALLPAPER";
    public static final String METHOD_SET_WALLPAPER_GIF = "SET_WALLPAPER_GIF";
    public static final String METHOD_SHARE = "SHARE";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isComplete = false;
    private AppCompatActivity activity;
    private ActivityDetailBinding binding;
    private ProgressLoader progressLoader;
    private final boolean isVertical = true;
    private int currentPosition = 0;
    private DatabaseHandler databaseHandler;
    private RecyclerView.LayoutManager layoutManager;

    protected void initView(AppCompatActivity activity, final ActivityDetailBinding binding) {
        this.activity = activity;
        this.binding = binding;
        progressLoader = new ProgressLoader(activity);
        Constant.ads_count++;
        if (Constant.ads_count == Constant.interstitial_click) {
            Constant.ads_count = 0;
        }
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        checkPremiumWallpaper(activity.getIntent().getIntExtra("POS", 0));
        sendViews(activity.getIntent().getIntExtra("POS", 0));
        loadBanner();
        initClickListener();

        activity.getApplication().getSharedPreferences("KenzAlmuslim", Context.MODE_PRIVATE);
        databaseHandler = new DatabaseHandler(activity);
        loadImages();
    }

    private void loadBanner() {
//        if (!CommonUtils.isNetworkConnected(activity))
//            return;
//        if (Constant.AdsOptions.IDENTIFIER.equals(Constant.AdsOptions.FACEBOOK)) {
//            AdView adView = new AdView(activity, Constant.facebook_banner_id, AdSize.BANNER_HEIGHT_50);
//            binding.adsView.addView(adView);
//            adView.loadAd();
//        } else {
//            com.google.android.gms.ads.AdView adView = new com.google.android.gms.ads.AdView(activity);
//            adView.setAdSize(com.google.android.gms.ads.AdSize.SMART_BANNER);
//            adView.setAdUnitId(Constant.banner_id);
//            binding.adsView.addView(adView);
//            adView.loadAd(new AdRequest.Builder().build());
//        }

    }

    private void initClickListener() {
        final List<ButtonData> buttonDatas = new ArrayList<>();
        final List<ButtonData> otherShares = new ArrayList<>();
        int[] drawable = {
                R.drawable.ic_detail_plus,
                R.drawable.ic_detail_zoom,
                R.drawable.ic_detail_download,
                R.drawable.ic_detail_crop,
                R.drawable.ic_detail_share,
        };
        int[] secondDrawables = {
                R.drawable.ic_detail_share,
                R.drawable.ic_messenger,
                R.drawable.ic_whatsapp,
        };
        for (int value : drawable) {
            ButtonData buttonData = ButtonData.buildIconButton(activity, value, 12);
            buttonData.setBackgroundColorId(activity, R.color.colorButtonDetail);
            buttonDatas.add(buttonData);
        }
        for (int value : secondDrawables) {
            ButtonData buttonData = ButtonData.buildIconButton(activity, value, 12);
            buttonData.setBackgroundColorId(activity, R.color.colorButtonDetail);
            otherShares.add(buttonData);
        }
        binding.buttonExpandable.setButtonDatas(buttonDatas);
        binding.buttonExpandable.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                switch (index) {
                    case 1:
                        Intent intent = new Intent(activity, ZoomActivity.class);
                        intent.putExtra("str_image", Constant.arrayListDetail.get(getCurrentPosition()).wallpaper_image_original);
                        activity.startActivity(intent);
                        break;
                    case 2:
                        wallpaperTask(METHOD_DOWNLOAD);
                        break;
                    case 3:
                        int pos = getCurrentPosition();
                        String wallType = (pos >= 0 && pos < Constant.arrayListDetail.size()) ? Constant.arrayListDetail.get(pos).wallpaper_type : "";
                        wallpaperTask("gif".equals(wallType) ? METHOD_SET_WALLPAPER_GIF : METHOD_SET_WALLPAPER);
                        break;
                    case 4:
                        wallpaperTask(METHOD_SHARE);
                        break;
                }
            }

            @Override
            public void onExpand() {

            }

            @Override
            public void onCollapse() {

            }
        });

        binding.otherShareExpandable.setButtonDatas(otherShares);
        binding.otherShareExpandable.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                switch (index) {
                    case 1:
                        shareImage("messenger");
                        break;
                    case 2:
                        shareImage("whatsapp");
                        break;
                }
            }

            @Override
            public void onExpand() {

            }

            @Override
            public void onCollapse() {

            }
        });
        final List<ButtonData> setAsBackgroundButton = new ArrayList<>();
        int[] setAsBackground = {
                R.drawable.ic_phone_background,
        };
        for (int value : setAsBackground) {
            ButtonData buttonData = ButtonData.buildIconButton(activity, value, 12);
            buttonData.setBackgroundColorId(activity, R.color.colorButtonDetail);
            setAsBackgroundButton.add(buttonData);
        }
        binding.asBackgroundExpandable.setButtonDatas(setAsBackgroundButton);
        binding.asBackgroundExpandable.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                // NO-OP
            }

            @Override
            public void onExpand() {
                shareImage("wallpaper");
            }

            @Override
            public void onCollapse() {
            }
        });
    }

    private void checkPremiumWallpaper(int position) {
        if (Constant.arrayListDetail.get(position).wallpaper_premium != null) {
            binding.layoutLocked.setVisibility(Constant.arrayListDetail.get(position).wallpaper_premium.equals("premium")
                    && !SharedPref.getSharedPref(activity).read("premium_checker")
                    .contains(Constant.arrayListDetail.get(position).wallpaper_image_original) ? View.VISIBLE : View.GONE);
            if (!Constant.arrayListDetail.get(position).wallpaper_premium.equals("premium")) {
//            binding.buttonExpandable.setVisibility(Constant.arrayListDetail.get(position).view_type == AdapterWallpaper.VIEW_NATIVE ? View.GONE : View.VISIBLE);
            }
        }
//        binding.buttonExpandable.setVisibility(Constant.arrayListDetail.get(position).wallpaper_premium.equals("premium") && !SharedPref.getSharedPref(activity).read("premium_checker").contains(Constant.arrayListDetail.get(position).wallpaper_image_original) ? View.GONE : View.VISIBLE);
    }

    private void loadImages() {
//        if (preferences.getString("scroll_type", "H").equals("H")) {
//            binding.changeOrientation.setChecked(true);
//            showHorizontal();
//            isVertical = false;
//        } else if (preferences.getString("scroll_type", "H").equals("V")) {
//            binding.changeOrientation.setChecked(false);
//            showVertical();
//            isVertical = true;
//        }
//        binding.changeOrientation.setOnCheckedChangeListener((compoundButton, b) -> {
//            if (compoundButton.isPressed() && b) {
//                preferences.edit().putString("scroll_type", "H").apply();
//                showHorizontal();
//            } else if (compoundButton.isPressed() && !b) {
//                preferences.edit().putString("scroll_type", "V").apply();
//                showVertical();
//            }
//        });
        showVertical();
    }

    private void showVertical() {
        VerticalAdapter adapter = new VerticalAdapter(activity);
        adapter.setDetails(true);
        adapter.setArrayList(Constant.arrayListDetail);
        binding.verticalViewPager.setAdapter(adapter);
        adapter.setOnPageItemSelected(this::checkPremiumWallpaper);
        binding.viewPager.setVisibility(View.GONE);
        binding.verticalViewPager.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        binding.verticalViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPosition = position;
                binding.likeButton.setLiked(databaseHandler.isFav(Constant.arrayListDetail.get(currentPosition).wall_id));
                binding.numViews.setText(String.valueOf(Constant.arrayListDetail.get(position).numViews));
                binding.numLikes.setText(String.valueOf(Constant.arrayListDetail.get(position).numLikes));
            }
        });
        binding.verticalViewPager.setCurrentItem(activity.getIntent().getIntExtra("POS", 0));
        binding.likeButton.setLiked(databaseHandler.isFav(Constant.arrayListDetail.get(currentPosition).wall_id));
        binding.likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                long likes = Constant.arrayListDetail.get(currentPosition).numLikes;
                likes++;
                binding.numLikes.setText(String.valueOf(likes));
                handler.post(() -> {
                    incrementLikes();
                    addFavourite();
                });
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                long likes = Constant.arrayListDetail.get(currentPosition).numLikes;
                likes--;
                binding.numLikes.setText(String.valueOf(likes));
                handler.post(() -> {
                    decrementLikes();
                    removeFavourite();
                });
            }
        });
    }

    private void incrementLikes() {
        if (!CommonUtils.isNetworkConnected(activity))
            return;
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "likes_increment");
        bsObject.addProperty("id", Constant.arrayListDetail.get(currentPosition).wall_id);
        new BSJson.Builder(activity)
                .setServer(Constant.SERVER_URL)
                .setObject(bsObject.getProperty())
                .setMethod(BSJson.METHOD_POST)
                .setListener(new BSJsonV2Listener() {
                    @Override
                    public void onLoaded(String response) {
                        Log.d("VerticalAdapter", "onLoaded: => " + response);
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("TAG", "onError: => " + error);
                    }
                }).load();
    }

    private void addFavourite() {
        int wall_id = Constant.arrayListDetail.get(currentPosition).wall_id;
        int cat_id = Constant.arrayListDetail.get(currentPosition).cat_id;
        String wallpaper_title = Constant.arrayListDetail.get(currentPosition).wallpaper_title;
        String wallpaper_image_thumb = Constant.arrayListDetail.get(currentPosition).wallpaper_image_thumb;
        String wallpaper_image_detail = Constant.arrayListDetail.get(currentPosition).wallpaper_image_detail;
        String wallpaper_image_original = Constant.arrayListDetail.get(currentPosition).wallpaper_image_original;
        String wallpaper_tags = Constant.arrayListDetail.get(currentPosition).wallpaper_tags;
        String wallpaper_type = Constant.arrayListDetail.get(currentPosition).wallpaper_type;
        String wallpaper_premium = Constant.arrayListDetail.get(currentPosition).wallpaper_premium;
        Long numLikes = Constant.arrayListDetail.get(currentPosition).numLikes;
        Long numViews = Constant.arrayListDetail.get(currentPosition).numViews;
        databaseHandler.AddtoFavorite(new ModelWallpaper(wall_id, cat_id, wallpaper_title, wallpaper_image_thumb, wallpaper_image_detail, wallpaper_image_original,
                wallpaper_tags, wallpaper_type, wallpaper_premium, AdapterWallpaper.VIEW_ITEM, numLikes, numViews));
    }

    private void removeFavourite() {
        databaseHandler.RemoveFav(Constant.arrayListDetail.get(currentPosition).wall_id);
    }

    private void decrementLikes() {
        if (!CommonUtils.isNetworkConnected(activity))
            return;
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "likes_decrement");
        bsObject.addProperty("id", Constant.arrayListDetail.get(currentPosition).wall_id);
        new BSJson.Builder(activity)
                .setServer(Constant.SERVER_URL)
                .setObject(bsObject.getProperty())
                .setMethod(BSJson.METHOD_POST)
                .setListener(new BSJsonV2Listener() {
                    @Override
                    public void onLoaded(String response) {
                        Log.d("VerticalAdapter", "onLoaded: => " + response);
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("TAG", "onError: => " + error);
                    }
                }).load();
    }

    private void showHorizontal() {
        binding.viewPager.setVisibility(View.VISIBLE);
        binding.verticalViewPager.setVisibility(View.GONE);

        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(activity, Constant.arrayListDetail);
        binding.viewPager.setAdapter(imagePagerAdapter);
        binding.viewPager.useScale();
        binding.viewPager.useAlpha();
        imagePagerAdapter.enableCarrousel();
        binding.viewPager.disableScroll(false);
        binding.viewPager.setCurrentItem(activity.getIntent().getIntExtra("POS", 0));
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                checkPremiumWallpaper(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private int getCurrentPosition() {
        if (binding != null && binding.verticalViewPager != null && binding.verticalViewPager.getVisibility() == View.VISIBLE) {
            return binding.verticalViewPager.getCurrentItem();
        } else if (binding != null && binding.viewPager != null && binding.viewPager.getVisibility() == View.VISIBLE) {
            return binding.viewPager.getCurrentItem();
        }
        return currentPosition;
    }

    private void wallpaperTask(String method) {
        WallPaperCommonUtils.wallpaperTask(method, activity, getCurrentPosition());
    }

    private void cropWallpaper() {
        WallPaperCommonUtils.cropWallpaper(activity, getCurrentPosition());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), resultUri);
                    WallPaperCommonUtils.showBottomSheetDialog(bitmap, activity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void shareImage(final String type) {
        WallPaperCommonUtils.shareImage(type, activity, getCurrentPosition());
    }

    private void sendViews(int position) {
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "send_view");
        bsObject.addProperty("id", Constant.arrayListDetail.get(position).wall_id);
        new BSJson.Builder(activity)
                .setServer(Constant.SERVER_URL)
                .setObject(bsObject.getProperty())
                .setMethod(BSJson.METHOD_POST)
                .setListener(new BSJsonV2Listener() {
                    @Override
                    public void onLoaded(String response) {

                    }

                    @Override
                    public void onError(String error) {

                    }
                })
                .load();
    }

    protected void onBackPressed() {
        activity.finish();
        activity.overridePendingTransition(R.anim.no_change, R.anim.slide_down);
    }

    @SuppressLint("StaticFieldLeak")
    public class SetWallpaperTask extends AsyncTask<String, String, String> {
        private final SweetAlertDialog pDialog;
        Bitmap bmImg;
        String options;

        @SuppressWarnings("deprecation")
        private SetWallpaperTask(Bitmap bmImg, String options) {
            this.bmImg = bmImg;
            this.options = options;
            pDialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText(activity.getString(R.string.please_wait));
            pDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            return null;
        }

        @Override
        protected void onPostExecute(String args) {
            pDialog.dismiss();
            WallpaperManager wpm = WallpaperManager.getInstance(activity.getApplicationContext());
            try {
                wpm.setWallpaperOffsetSteps(0, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    switch (options) {
                        case "HOME":
                            wpm.setBitmap(bmImg, null, true, WallpaperManager.FLAG_SYSTEM);
                            break;
                        case "LOCK":
                            wpm.setBitmap(bmImg, null, true, WallpaperManager.FLAG_LOCK);
                            break;
                        case "BOTH":
                            wpm.setBitmap(bmImg, null, true, WallpaperManager.FLAG_LOCK);
                            wpm.setBitmap(bmImg, null, true, WallpaperManager.FLAG_SYSTEM);
                            break;
                    }
                } else {
                    wpm.setBitmap(bmImg);
                }
                DialogDone dialogDone = DialogDone.getInstance(activity.getString(R.string.image_set_done), activity.getString(R.string.button_ok));
                dialogDone.setOnOkClickListener(dialogDone::dismiss);
                dialogDone.show(activity.getSupportFragmentManager(), DialogDone.TAG);
            } catch (Exception e) {
                e.printStackTrace();
                new SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("حدث خطأ ما في عملية تعيين الصورة")
                        .setConfirmText("حسنا")
                        .setConfirmClickListener(sweetAlertDialog -> activity.finish())
                        .show();
            }

        }
    }
}