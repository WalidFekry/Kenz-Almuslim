package com.kenz.almuslim.data.widgets;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.kenz.almuslim.R;


public class CustomDialog extends Dialog {
    private String text_message;
    private String text_title;
    private String btYesText;
    private String btNoText;
    private int dialog_type;
    private View.OnClickListener btYesListener = null;
    private View.OnClickListener btNoListener = null;
    public static final int ONE_BUTTON = 1;
    public static final int AUTO_DISMISS = 2;
    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.custom_dialog);
        TextView title = findViewById(R.id.dialog_title);
        TextView message = findViewById(R.id.dialog_message);
        TextView button_yes =  findViewById(R.id.button_yes);
        TextView button_no = findViewById(R.id.button_no);
        if(dialog_type == ONE_BUTTON){
            button_yes.setVisibility(View.GONE);
            button_no.setText("OKE");
            button_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        } else if(dialog_type == AUTO_DISMISS){
            button_no.setVisibility(View.GONE);
            button_yes.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 2000);
        }else {
            button_no.setOnClickListener(btNoListener);
            button_no.setText(btNoText);
        }
        title.setText(text_title);
        message.setText(text_message);
        button_yes.setOnClickListener(btYesListener);
        button_yes.setText(btYesText);

    }


    public void setTitle(String text_title) {
        this.text_title = text_title;
    }

    public void setMessage(String text_message) {
        this.text_message = text_message;
    }

    public void setDialogType(int dialog_type) {
        this.dialog_type = dialog_type;
    }
    public void setPositveButton(String yes, View.OnClickListener onClickListener) {
        dismiss();
        this.btYesText = yes;
        this.btYesListener = onClickListener;
    }

    public void setNegativeButton(String yes, View.OnClickListener onClickListener) {
        dismiss();
        this.btNoText = yes;
        this.btNoListener = onClickListener;
    }
}
