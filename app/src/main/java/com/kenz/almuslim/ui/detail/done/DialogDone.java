package com.kenz.almuslim.ui.detail.done;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.kenz.almuslim.R;

import org.jetbrains.annotations.NotNull;

public class DialogDone extends DialogFragment {

    public static final String TEXT_KEY = "com.kenz.almuslim.ui.detail.done_Text";
    public static final String BUTTON_KEY = "com.kenz.almuslim.ui.detail.done_Button";
    public static final String TAG = "DialogDone";

    @NotNull
    public static DialogDone getInstance(String messageBody, String buttonText) {
        DialogDone dialogDone = new DialogDone();
        Bundle bundle = new Bundle();
        bundle.putString(TEXT_KEY, messageBody);
        bundle.putString(BUTTON_KEY, buttonText);
        dialogDone.setArguments(bundle);
        return dialogDone;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final RelativeLayout root = new RelativeLayout(getActivity());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        root.setLayoutParams(params);
        final Dialog dialog = new Dialog(getContext());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.base_dialog_bg, null));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private OnOkClickListener onOkClickListener;

    public void setOnOkClickListener(OnOkClickListener onOkClickListener) {
        this.onOkClickListener = onOkClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_done, container, false);
        AppCompatTextView message = view.findViewById(R.id.dialog_message);
        AppCompatButton done = view.findViewById(R.id.button_ok);
        if (!getArguments().getString(TEXT_KEY).isEmpty()) {
            message.setText(getArguments().getString(TEXT_KEY));
        }
        if (!getArguments().getString(BUTTON_KEY).isEmpty()) {
            done.setText(getArguments().getString(BUTTON_KEY));
        }
        done.setOnClickListener(v -> {
            if (onOkClickListener != null) {
                onOkClickListener.onOkClick();
            }
        });
        return view;
    }


    public interface OnOkClickListener {
        void onOkClick();
    }
}
