package com.kenz.almuslim.ui.splash;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.kenz.almuslim.R;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class DialogWhy extends DialogFragment {

    public static final String TAG = "DialogWhy";

    @Contract(" -> new")
    public static @NotNull DialogWhy getInstance() {
        return new DialogWhy();
    }

    private OnWheyListener listener;

    public void setListener(OnWheyListener listener) {
        this.listener = listener;
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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_why, container, false);
        AppCompatButton button = view.findViewById(R.id.button_ok);
        button.setOnClickListener(v -> listener.onOkClick());
        return view;
    }


    public interface OnWheyListener {
        void onOkClick();
    }

}
