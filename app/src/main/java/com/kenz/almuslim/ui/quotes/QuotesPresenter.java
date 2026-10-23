package com.kenz.almuslim.ui.quotes;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.model.ModelQuote;
import com.kenz.almuslim.data.utils.QuotesDatabaseHandler;
import com.kenz.almuslim.utils.CommonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class QuotesPresenter extends BasePresenter<QuotesView> {

    private static final String API_URL = "https://post.walid-fekry.com/auto_notification/auto_notification_apps/api_post_apps.php";

    private Activity activity;
    private QuotesDatabaseHandler dbHandler;
    private ArrayList<ModelQuote> quotesList = new ArrayList<>();

    public void init(Activity activity) {
        this.activity = activity;
        this.dbHandler = new QuotesDatabaseHandler(activity);
    }

    public void loadQuotes() {
        if (getMvpView() != null) {
            getMvpView().showLoading(true);
        }

        if (CommonUtils.isNetworkConnected(activity)) {
            AndroidNetworking.get(API_URL)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            parseAndDisplayQuotes(response);
                        }

                        @Override
                        public void onError(ANError anError) {
                            loadOfflineCachedQuotes();
                        }
                    });
        } else {
            loadOfflineCachedQuotes();
        }
    }

    private void parseAndDisplayQuotes(String response) {
        try {
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            if (jsonObject.has("post") && jsonObject.get("post").isJsonArray()) {
                JsonArray array = jsonObject.getAsJsonArray("post");
                ArrayList<ModelQuote> fetchedList = new ArrayList<>();

                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    int id = obj.has("id") ? obj.get("id").getAsInt() : 0;
                    String title = obj.has("title") ? obj.get("title").getAsString() : "";
                    String createdAt = obj.has("created_at") ? obj.get("created_at").getAsString() : "";
                    String imageUrl = obj.has("imageurl") ? obj.get("imageurl").getAsString() : "";

                    boolean isSaved = dbHandler.isQuoteSaved(id);
                    fetchedList.add(new ModelQuote(id, title, createdAt, imageUrl, isSaved));
                }

                // Reverse the list received from the API
                Collections.reverse(fetchedList);

                quotesList = fetchedList;
                dbHandler.cacheQuotes(quotesList);

                if (getMvpView() != null) {
                    getMvpView().showLoading(false);
                    if (quotesList.isEmpty()) {
                        getMvpView().showEmptyOrOfflineView();
                    } else {
                        getMvpView().showQuotes(quotesList);
                    }
                }
            } else {
                loadOfflineCachedQuotes();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadOfflineCachedQuotes();
        }
    }

    private void loadOfflineCachedQuotes() {
        ArrayList<ModelQuote> cached = dbHandler.getCachedQuotes();
        if (cached == null || cached.isEmpty()) {
            cached = dbHandler.getAllSavedQuotes();
        }

        if (getMvpView() != null) {
            getMvpView().showLoading(false);
            if (cached != null && !cached.isEmpty()) {
                // Display offline cached / saved quotes in random order
                Collections.shuffle(cached);
                quotesList = cached;
                getMvpView().showQuotes(quotesList);
                getMvpView().showToast("تصفح الوضع الأوفلاين (رسائل عشوائية)");
            } else {
                getMvpView().showEmptyOrOfflineView();
            }
        }
    }

    public void toggleSaveQuote(ModelQuote quote, int position) {
        boolean newSavedState = !quote.isSaved;
        quote.isSaved = newSavedState;

        if (newSavedState) {
            dbHandler.saveQuote(quote);
            if (getMvpView() != null) {
                getMvpView().showToast("تم حفظ الرسالة بنجاح 📌");
            }
        } else {
            dbHandler.removeSavedQuote(quote.id);
            if (getMvpView() != null) {
                getMvpView().showToast("تم إزالة الرسالة من المحفوظات");
            }
        }
    }

    public void copyQuoteText(ModelQuote quote) {
        if (quote == null || quote.title == null) return;
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Quote", quote.title);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            if (getMvpView() != null) {
                getMvpView().showToast("تم نسخ نص الرسالة بنجاح 📋");
            }
        }
    }

    public void shareWhatsApp(ModelQuote quote) {
        if (quote == null || quote.title == null) return;
        String message = quote.title + "\n\n"
                + "تطبيق كنز المسلم | Kenz Almuslim 🕌\n"
                + "https://play.google.com/store/apps/details?id=" + activity.getPackageName();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage("com.whatsapp");
        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            shareGeneralText(quote);
            if (getMvpView() != null) {
                getMvpView().showToast("يرجى تثبيت تطبيق الواتساب أولاً");
            }
        }
    }

    public void shareMessenger(ModelQuote quote) {
        if (quote == null || quote.title == null) return;
        String message = quote.title + "\n\n"
                + "تطبيق كنز المسلم | Kenz Almuslim 🕌\n"
                + "https://play.google.com/store/apps/details?id=" + activity.getPackageName();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage("com.facebook.orca");
        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            shareGeneralText(quote);
            if (getMvpView() != null) {
                getMvpView().showToast("يرجى تثبيت تطبيق الماسنجر أولاً");
            }
        }
    }

    public void shareGeneralText(ModelQuote quote) {
        if (quote == null || quote.title == null) return;
        String message = quote.title + "\n\n"
                + "تطبيق كنز المسلم | Kenz Almuslim 🕌\n"
                + "https://play.google.com/store/apps/details?id=" + activity.getPackageName();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "رسالة من كنز المسلم");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        activity.startActivity(Intent.createChooser(intent, "مشاركة الرسالة:"));
    }

    public void saveQuoteAsImage(ModelQuote quote) {
        if (quote == null || quote.title == null) return;

        try {
            View view = LayoutInflater.from(activity).inflate(R.layout.layout_quote_card_image, null);

            TextView tvBadge = view.findViewById(R.id.tv_export_badge);
            TextView tvText = view.findViewById(R.id.tv_export_text);
            TextView tvDate = view.findViewById(R.id.tv_export_date);

            tvBadge.setText("رسالة #" + quote.id);
            tvText.setText(quote.title);
            tvDate.setText(quote.createdAt != null ? quote.createdAt : "");

            view.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);

            String fileName = "Quote_" + quote.id + "_" + System.currentTimeMillis() + ".jpg";
            saveBitmapToGallery(bitmap, fileName);

        } catch (Exception e) {
            e.printStackTrace();
            if (getMvpView() != null) {
                getMvpView().showToast("حدث خطأ أثناء حفظ الصورة");
            }
        }
    }

    private void saveBitmapToGallery(Bitmap bitmap, String fileName) {
        OutputStream outputStream = null;
        File imageFile = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "KenzAlmuslim");

                Uri uri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    outputStream = activity.getContentResolver().openOutputStream(uri);
                }
            } else {
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File dir = new File(picturesDir, "KenzAlmuslim");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                imageFile = new File(dir, fileName);
                outputStream = new FileOutputStream(imageFile);
            }

            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                if (imageFile != null) {
                    MediaScannerConnection.scanFile(activity, new String[]{imageFile.getAbsolutePath()}, new String[]{"image/jpeg"}, null);
                }

                if (getMvpView() != null) {
                    getMvpView().showToast("تم حفظ الاقتباس كصورة بنجاح في المعرض 🖼️");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (getMvpView() != null) {
                getMvpView().showToast("حدث خطأ أثناء حفظ الصورة");
            }
        }
    }
}
