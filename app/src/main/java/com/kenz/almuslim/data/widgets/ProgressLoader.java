package com.kenz.almuslim.data.widgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;

import com.kenz.almuslim.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;



public class ProgressLoader extends AlertDialog {

    public ProgressLoader(@NonNull Context context) {
        super(context);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }


    @Override
    public void show() {
        super.show();
        setContentView(R.layout.progress_layout);
        setCancelable(false);
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    public void stopLoader() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, 500);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
