package com.kenz.almuslim.ui.quotes.saved;

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

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.model.ModelQuote;
import com.kenz.almuslim.data.utils.QuotesDatabaseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SavedQuotesPresenter extends BasePresenter<SavedQuotesView> {

    private Activity activity;
    private QuotesDatabaseHandler dbHandler;

    public void init(Activity activity) {
        this.activity = activity;
        this.dbHandler = new QuotesDatabaseHandler(activity);
    }

    public void loadSavedQuotes() {
        ArrayList<ModelQuote> savedQuotes = dbHandler.getAllSavedQuotes();
        if (getMvpView() != null) {
            if (savedQuotes == null || savedQuotes.isEmpty()) {
                getMvpView().showNoSavedQuotesView();
            } else {
                getMvpView().showSavedQuotes(savedQuotes);
            }
        }
    }

    public void deleteSavedQuote(ModelQuote quote) {
        if (quote == null) return;
        dbHandler.removeSavedQuote(quote.id);
        if (getMvpView() != null) {
            getMvpView().showToast("تم إزالة الرسالة من المحفوظات");
        }
        loadSavedQuotes();
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
