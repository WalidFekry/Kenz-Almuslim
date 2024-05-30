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
            if (Constant.AdsOptions.IDENTIFIER.equals(Constant.AdsOptions.FACEBOOK)) {

            } else {

            }

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
                        intent.putExtra("str_image", Constant.arrayListDetail.get(currentPosition).wallpaper_image_original);
                        activity.startActivity(intent);
                        break;
                    case 2:
                        wallpaperTask(METHOD_DOWNLOAD);
                        break;
                    case 3:
                        wallpaperTask(Constant.arrayListDetail.get(binding.viewPager.getCurrentItem()).wallpaper_type.equals("gif") ? METHOD_SET_WALLPAPER_GIF : METHOD_SET_WALLPAPER);
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


    private void wallpaperTask(String method) {
        switch (method) {
            case METHOD_DOWNLOAD:
                shareImage("download");
                break;
            case METHOD_SHARE:
                shareImage("share");
                break;
            case METHOD_SET_WALLPAPER:
                cropWallpaper();
                break;
            case METHOD_SET_WALLPAPER_GIF:
                new BSDownloadFile.Builder(activity)
                        .setUrl(Constant.arrayListDetail.get(binding.viewPager.getCurrentItem()).wallpaper_image_original)
                        .setDirectory(activity.getExternalCacheDir() + File.separator + "set_wallpaper" + File.separator)
                        .setListener((directory, filename) -> {
                            SharedPref.getSharedPref(activity).write("set_wallpaper", directory + filename);
                            Intent intent;
                            try {
                                WallpaperManager.getInstance(activity).clear();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(activity, activity.getPackageName() + ".data.services.GifWallpaperService"));
                                intent.putExtra("SET_LOCKSCREEN_WALLPAPER", true);
                                activity.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(activity, "هاتفكم لايدعم هذه الخدمة", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .download();
                break;
        }
    }

    private void cropWallpaper() {
        int p = binding.viewPager.getCurrentItem() + 1;
        Uri source = Uri.parse(Constant.arrayListDetail.get(p).wallpaper_image_original);
        File dest = new File(activity.getApplicationContext().getCacheDir(), "cropped");
        //noinspection ResultOfMethodCallIgnored
        dest.mkdirs();
        Uri destination = Uri.fromFile(new File(activity.getApplicationContext().getCacheDir(), "cropped/image.png"));
        UCrop uCrop = UCrop.of(source, destination);
        uCrop = basisConfig(uCrop);
        uCrop = advancedConfig(uCrop);
        uCrop.start(activity);
    }

    private UCrop basisConfig(@NonNull UCrop uCrop) {
        return uCrop.useSourceImageAspectRatio();
    }

    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        return uCrop.withOptions(options);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), resultUri);
                if (Constant.AdsOptions.IDENTIFIER.equals(Constant.AdsOptions.FACEBOOK)) {
                } else {
                }
                showBottomSheetDialog(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showBottomSheetDialog(final Bitmap bitmap) {
        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_set_wallpaper, null);
        final BottomSheetDialog dialog_desc = new BottomSheetDialog(activity, R.style.SheetDialog);
        dialog_desc.setCancelable(true);
        dialog_desc.setContentView(view);
        dialog_desc.show();

        TextView home_screen = dialog_desc.findViewById(R.id.home_screen);
        TextView lock_screen = dialog_desc.findViewById(R.id.lock_screen);
        TextView home_lock_screen = dialog_desc.findViewById(R.id.home_lock_screen);

        Objects.requireNonNull(home_screen).setOnClickListener(v -> {
            dialog_desc.dismiss();
            new SetWallpaperTask(bitmap, "HOME").execute();
        });

        Objects.requireNonNull(lock_screen).setOnClickListener(v -> {
            dialog_desc.dismiss();
            new SetWallpaperTask(bitmap, "LOCK").execute();
        });

        Objects.requireNonNull(home_lock_screen).setOnClickListener(v -> {
            dialog_desc.dismiss();
            new SetWallpaperTask(bitmap, "BOTH").execute();
        });
    }

    private void shareImage(final String type) {
        String shareImage = null;
        if (CommonUtils.isNetworkConnected(activity)) {
            if (isVertical) {
                shareImage = Constant.arrayListDetail.get(binding.verticalViewPager.getCurrentItem()).wallpaper_image_original;
            } else
                shareImage = Constant.arrayListDetail.get(binding.viewPager.getCurrentItem()).wallpaper_image_original;
        } else {
            shareFromLocalImage(type);
            return;
        }
        String finalShareImage = shareImage;
        Picasso.get()
                .load(shareImage)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        String root = null;
                        switch (type) {
                            case "whatsapp":
                            case "messenger":
                            case "share":
                                root = activity.getExternalCacheDir() + File.separator;
                                break;
                            case "download":
                            case "wallpaper":
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
                                } else {

                                    root = Environment.getExternalStorageDirectory().getAbsolutePath();
                                }

                                root = root +
                                        File.separator
                                        + activity.getString(R.string.download_folder)
                                        + File.separator;

                                File rootFile = new File(root);
                                if (!rootFile.exists()) {
                                    rootFile.mkdir();
                                }
                                break;
                        }
                        final File directory = new File(root, finalShareImage.substring(finalShareImage.lastIndexOf("/")));
                        try {
                            if (!directory.exists()) {
                                FileOutputStream out = new FileOutputStream(directory);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                out.flush();
                                out.close();
                            }
                            switch (type) {
                                case "share":
                                    Uri shareUri = FileProvider.getUriForFile(activity, "com.kenz.almuslim.fileprovider", directory);
                                    if (shareUri != null) {
                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.get_more_wall)
                                                + "\n"
                                                + activity.getString(R.string.app_name)
                                                + " - "
                                                + "https://t.co/w1AIQWabTc?"
                                                + activity.getPackageName());
                                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                                        shareIntent.setDataAndType(shareUri, activity.getContentResolver().getType(shareUri));
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                                        activity.startActivity(Intent.createChooser(shareIntent, "Choose an app"));

                                    }
                                    break;
                                case "download":
                                    MediaScannerConnection.scanFile(activity, new String[]{directory.getAbsolutePath()},
                                            null,
                                            (path, uri) -> {

                                            });
                                    Toast.makeText(activity, "تم تحميل الصورة: " + directory, Toast.LENGTH_SHORT).show();
                                    break;

                                case "whatsapp":
                                    Log.d("onBitmapLoadedonBitm", "onBitmapLoaded: "+directory.getName());
                                    Uri contentUri = FileProvider.getUriForFile(activity, "com.kenz.almuslim.fileprovider", directory);
                                    if (contentUri != null) {

                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.setPackage("com.whatsapp");
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.get_more_wall)
                                                + "\n"
                                                + activity.getString(R.string.app_name)
                                                + " - "
                                                + "https://t.co/w1AIQWabTc?"
                                                + activity.getPackageName());
                                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                                        shareIntent.setDataAndType(contentUri, activity.getContentResolver().getType(contentUri));
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                        activity.startActivity(Intent.createChooser(shareIntent, "Choose an app"));

                                    }
                                    break;

                                case "messenger":
                                    Uri image = FileProvider.getUriForFile(activity, "com.kenz.almuslim.fileprovider", directory);
                                    Intent messengerIntent = new Intent(Intent.ACTION_SEND);
                                    messengerIntent.setType("text/plain");
                                    messengerIntent.setPackage("com.facebook.orca");
                                    messengerIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.get_more_wall)
                                            + "\n"
                                            + activity.getString(R.string.app_name)
                                            + " - "
                                            + "https://t.co/w1AIQWabTc?"
                                            + activity.getPackageName());
                                    messengerIntent.setDataAndType(image, activity.getContentResolver().getType(image));
                                    messengerIntent.setType("image/jpeg");
                                    try {
                                        activity.startActivity(messengerIntent);
                                    } catch (android.content.ActivityNotFoundException ex) {
                                        activity.runOnUiThread(() -> Toast.makeText(activity,
                                                activity.getString(R.string.please_install_messenger),
                                                Toast.LENGTH_SHORT).show());
                                    }

                                    break;

                                case "wallpaper":
                                    showBottomSheetDialog(bitmap);
                                    break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("onBitmapLoaded", "onBitmapLoaded: " + e.getMessage());
                            Log.d("onBitmapLoaded", "onBitmapLoaded: " + e.getLocalizedMessage());
                            Toast.makeText(activity, "an error occurred, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }


    private void shareFromLocalImage(String type) {
        Bitmap bitmap = null;
        if (isVertical)
//            bitmap = Constant.arrayListDetail.get(binding.verticalViewPager.getCurrentItem()).imageOriginal;
//        else
            bitmap = Constant.arrayListDetail.get(binding.viewPager.getCurrentItem()).imageOriginal;
        String root = null;
        switch (type) {
            case "whatsapp":
            case "messenger":
            case "share":
                root = activity.getExternalCacheDir() + File.separator;
                break;
            case "download":
            case "wallpaper":
                root = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator
                        + activity.getString(R.string.download_folder)
                        + File.separator;
                File rootFile = new File(root);
                if (!rootFile.exists()) {
                    rootFile.mkdir();
                }
                break;
        }
        final File directory = new File(root, "Temp_Image_name.png");
        try {
            if (!directory.exists()) {
                FileOutputStream out = new FileOutputStream(directory);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
            }
            switch (type) {
                case "share":
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + directory));
                    share.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.get_more_wall)
                            + "\n"
                            + activity.getString(R.string.app_name)
                            + " - "
                            + "https://t.co/w1AIQWabTc?"
                            + activity.getPackageName());
                    activity.startActivity(Intent.createChooser(share, activity.getString(R.string.share_image)));
                    break;
                case "download":
                    MediaScannerConnection.scanFile(activity, new String[]{directory.getAbsolutePath()},
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {

                                }
                            });
                    Toast.makeText(activity, "Downloaded at: " + directory, Toast.LENGTH_SHORT).show();
                    break;

                case "whatsapp":
                    Uri imageUri = Uri.parse(directory.getAbsolutePath());
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.whatsapp");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.get_more_wall)
                            + "\n"
                            + activity.getString(R.string.app_name)
                            + " - "
                            + "https://t.co/w1AIQWabTc?"
                            + activity.getPackageName());
                    whatsappIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    whatsappIntent.setType("image/jpeg");
                    whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        activity.startActivity(whatsappIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, activity.getString(R.string.please_install_wtsp), Toast.LENGTH_SHORT).show());
                    }
                    break;

                case "messenger":
                    Uri image = Uri.fromFile(directory);
                    Intent messengerIntent = new Intent(Intent.ACTION_SEND);
                    messengerIntent.setType("text/plain");
                    messengerIntent.setPackage("com.facebook.orca");
                    messengerIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.get_more_wall)
                            + "\n"
                            + activity.getString(R.string.app_name)
                            + " - "
                            + "https://t.co/w1AIQWabTc?"
                            + activity.getPackageName());
                    messengerIntent.putExtra(Intent.EXTRA_STREAM, image);
                    messengerIntent.setType("image/jpeg");
                    try {
                        activity.startActivity(messengerIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        activity.runOnUiThread(() -> Toast.makeText(activity,
                                activity.getString(R.string.please_install_messenger),
                                Toast.LENGTH_SHORT).show());
                    }
                    break;

                case "wallpaper":
                    showBottomSheetDialog(bitmap);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, "an error occurred, please try again later", Toast.LENGTH_SHORT).show();
        }
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