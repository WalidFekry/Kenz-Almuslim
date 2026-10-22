package com.kenz.almuslim.utils;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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
        if (position < 0 || position >= Constant.arrayListDetail.size()) {
            return;
        }

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
                            try {
                                WallpaperManager.getInstance(activity).clear();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(activity, activity.getPackageName() + ".data.services.GifWallpaperService"));
                                intent.putExtra("SET_LOCKSCREEN_WALLPAPER", true);
                                activity.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(activity, "هاتفكم لا يدعم هذه الخدمة", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .download();
                break;
            case METHOD_SHARE:
                shareImage("share", activity, position);
                break;
        }
    }

    public static void shareImage(String type, AppCompatActivity activity, int position) {
        if (position < 0 || position >= Constant.arrayListDetail.size()) {
            return;
        }

        String shareImage = null;
        if (CommonUtils.isNetworkConnected(activity)) {
            shareImage = Constant.arrayListDetail.get(position).wallpaper_image_original;
        } else {
            shareFromLocalImage(type, activity, position);
            return;
        }

        if (shareImage == null || shareImage.isEmpty()) {
            shareFromLocalImage(type, activity, position);
            return;
        }

        String finalShareImage = shareImage;
        Picasso.get()
                .load(shareImage)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        handleImageAction(type, activity, bitmap, finalShareImage.substring(finalShareImage.lastIndexOf("/") + 1));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText(activity, "حدث خطأ أثناء تحميل الصورة", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
    }

    public static void shareFromLocalImage(@NotNull String type, AppCompatActivity activity, int position) {
        if (position < 0 || position >= Constant.arrayListDetail.size()) {
            return;
        }
        Bitmap bitmap = Constant.arrayListDetail.get(position).imageOriginal;
        if (bitmap == null) {
            Toast.makeText(activity, "الصورة غير متوفرة", Toast.LENGTH_SHORT).show();
            return;
        }
        handleImageAction(type, activity, bitmap, "Image_" + System.currentTimeMillis() + ".png");
    }

    private static void handleImageAction(String type, AppCompatActivity activity, Bitmap bitmap, String fileName) {
        File directory;
        if ("download".equals(type) || "wallpaper".equals(type)) {
            File publicPicturesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            directory = new File(publicPicturesFolder, activity.getString(R.string.download_folder));
            if (!directory.exists()) {
                directory.mkdirs();
            }
        } else {
            directory = new File(activity.getExternalCacheDir(), "shared_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }

        File imageFile = new File(directory, fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ? fileName : fileName + ".jpg");

        try {
            if (!imageFile.exists()) {
                FileOutputStream out = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                out.flush();
                out.close();
            }

            Uri contentUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", imageFile);

            switch (type) {
                case "share":
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/jpeg");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.get_more_wall)
                            + "\n"
                            + activity.getString(R.string.app_name)
                            + " - "
                            + "https://play.google.com/store/apps/details?id="
                            + activity.getPackageName());
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.share_image)));
                    break;

                case "download":
                    MediaScannerConnection.scanFile(activity, new String[]{imageFile.getAbsolutePath()},
                            new String[]{"image/jpeg"},
                            (path, uri) -> activity.runOnUiThread(() ->
                                    Toast.makeText(activity, "تم حفظ الصورة بنجاح في الاستوديو", Toast.LENGTH_SHORT).show()));
                    break;

                case "whatsapp":
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("image/jpeg");
                    whatsappIntent.setPackage("com.whatsapp");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.get_more_wall)
                            + "\n"
                            + activity.getString(R.string.app_name)
                            + " - "
                            + "https://play.google.com/store/apps/details?id="
                            + activity.getPackageName());
                    whatsappIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        activity.startActivity(whatsappIntent);
                    } catch (ActivityNotFoundException ex) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, activity.getString(R.string.please_install_wtsp), Toast.LENGTH_SHORT).show());
                    }
                    break;

                case "messenger":
                    Intent messengerIntent = new Intent(Intent.ACTION_SEND);
                    messengerIntent.setType("image/jpeg");
                    messengerIntent.setPackage("com.facebook.orca");
                    messengerIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.get_more_wall)
                            + "\n"
                            + activity.getString(R.string.app_name)
                            + " - "
                            + "https://play.google.com/store/apps/details?id="
                            + activity.getPackageName());
                    messengerIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    messengerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        activity.startActivity(messengerIntent);
                    } catch (ActivityNotFoundException ex) {
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
            Toast.makeText(activity, "حدث خطأ، يرجى المحاولة لاحقاً", Toast.LENGTH_SHORT).show();
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
        new Thread(() -> {
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
                            wpm.setBitmap(bmImg, null, true, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                            break;
                    }
                } else {
                    wpm.setBitmap(bmImg);
                }
                activity.runOnUiThread(() -> {
                    try {
                        DialogDone dialogDone = DialogDone.getInstance(activity.getString(R.string.image_set_done), activity.getString(R.string.button_ok));
                        dialogDone.setOnOkClickListener(dialogDone::dismiss);
                        dialogDone.show(activity.getSupportFragmentManager(), DialogDone.TAG);
                    } catch (Exception e) {
                        Toast.makeText(activity, activity.getString(R.string.image_set_done), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> new SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("حدث خطأ ما في عملية تعيين الصورة")
                        .setConfirmText("حسنا")
                        .setConfirmClickListener(SweetAlertDialog::dismiss)
                        .show());
            }
        }).start();
    }

    public static void cropWallpaper(@NotNull AppCompatActivity activity, int position) {
        if (position < 0 || position >= Constant.arrayListDetail.size()) {
            return;
        }

        String imageUrl = Constant.arrayListDetail.get(position).wallpaper_image_original;
        if (CommonUtils.isNetworkConnected(activity) && imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    startCropWithBitmap(activity, bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Toast.makeText(activity, "فشل تحميل الصورة للقص", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });
        } else {
            Bitmap bitmap = Constant.arrayListDetail.get(position).imageOriginal;
            if (bitmap != null) {
                startCropWithBitmap(activity, bitmap);
            }
        }
    }

    private static void startCropWithBitmap(AppCompatActivity activity, Bitmap bitmap) {
        try {
            File sourceFile = new File(activity.getCacheDir(), "source_crop.png");
            FileOutputStream out = new FileOutputStream(sourceFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            File destFolder = new File(activity.getCacheDir(), "cropped");
            destFolder.mkdirs();
            Uri destination = Uri.fromFile(new File(destFolder, "image.png"));

            UCrop uCrop = UCrop.of(Uri.fromFile(sourceFile), destination);
            uCrop = uCrop.useSourceImageAspectRatio();
            UCrop.Options options = new UCrop.Options();
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            uCrop.withOptions(options).start(activity);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "حدث خطأ أثناء إعداد الصورة للقص", Toast.LENGTH_SHORT).show();
        }
    }
}
