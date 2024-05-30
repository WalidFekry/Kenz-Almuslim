package com.kenz.almuslim.utils;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.downloadservice.BSDownloadFile;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.SharedPref;
import com.kenz.almuslim.ui.detail.done.DialogDone;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import static com.kenz.almuslim.ui.detail.DetailPresenter.METHOD_SET_WALLPAPER;
import static com.kenz.almuslim.ui.detail.DetailPresenter.METHOD_SET_WALLPAPER_GIF;
import static com.kenz.almuslim.ui.detail.DetailPresenter.METHOD_SHARE;

public final class WallPaperCommonUtils {

    @Contract(pure = true)
    public static void wallpaperTask(@NotNull String what, AppCompatActivity activity, int position) {
        if (what.equals("DOWNLOAD")) {
            shareImage("download", activity, position);
            return;
        }

        switch (what) {
            case METHOD_SET_WALLPAPER:
                cropWallpaper(activity, position);
                break;
            case METHOD_SET_WALLPAPER_GIF:
                new BSDownloadFile.Builder(activity)
                        .setUrl(Constant.arrayListDetail.get(position).wallpaper_image_original)
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
            case METHOD_SHARE:
                shareImage("share", activity, position);
                break;
        }
    }


    public static void shareImage(String type, AppCompatActivity activity, int position) {
        String shareImage = null;
        if (CommonUtils.isNetworkConnected(activity)) {
            shareImage = Constant.arrayListDetail.get(position).wallpaper_image_original;
        } else {
            shareFromLocalImage(type, activity, position);
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
                                root = Environment.getExternalStorageDirectory()
                                        .getAbsolutePath()
                                        + File.separator
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
                                            (path, uri) -> {

                                            });
                                    Toast.makeText(activity, "تم تحميل الصورة: " + directory, Toast.LENGTH_SHORT).show();
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
                                        activity.runOnUiThread(() -> {
                                            Toast.makeText(activity, activity.getString(R.string.please_install_wtsp), Toast.LENGTH_SHORT).show();
                                        });
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
                                    showBottomSheetDialog(bitmap, activity);
                                    break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
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


    public static void shareFromLocalImage(@NotNull String type, AppCompatActivity activity, int position) {
        Bitmap bitmap = null;
        bitmap = Constant.arrayListDetail.get(position).imageOriginal;
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
                    showBottomSheetDialog(bitmap, activity);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, "an error occurred, please try again later", Toast.LENGTH_SHORT).show();
        }
    }


    public static void showBottomSheetDialog(final Bitmap bitmap, @NotNull AppCompatActivity activity) {
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
            setWallpaper("HOME", activity, bitmap);
        });

        Objects.requireNonNull(lock_screen).setOnClickListener(v -> {
            dialog_desc.dismiss();
            setWallpaper("LOCK", activity, bitmap);
        });

        Objects.requireNonNull(home_lock_screen).setOnClickListener(v -> {
            dialog_desc.dismiss();
            setWallpaper("BOTH", activity, bitmap);
        });
    }

    public static void setWallpaper(String type, AppCompatActivity activity, Bitmap bmImg) {
        WallpaperManager wpm = WallpaperManager.getInstance(activity.getApplicationContext());
        try {
            wpm.setWallpaperOffsetSteps(0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                switch (type) {
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


    public static void cropWallpaper(@NotNull AppCompatActivity activity, int position) {
        Uri source = Uri.parse(Constant.arrayListDetail.get(position).wallpaper_image_original);
        File dest = new File(activity.getApplicationContext().getCacheDir(), "cropped");
        //noinspection ResultOfMethodCallIgnored
        dest.mkdirs();
        Uri destination = Uri.fromFile(new File(activity.getApplicationContext().getCacheDir(), "cropped/image.png"));
        UCrop uCrop = UCrop.of(source, destination);
        uCrop = basisConfig(uCrop);
        uCrop = advancedConfig(uCrop);
        uCrop.start(activity);
    }


    public static UCrop basisConfig(@NonNull UCrop uCrop) {
        return uCrop.useSourceImageAspectRatio();
    }

    public static UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        return uCrop.withOptions(options);
    }

}
