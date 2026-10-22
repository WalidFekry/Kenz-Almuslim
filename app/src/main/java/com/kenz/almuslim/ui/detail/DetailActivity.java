package com.kenz.almuslim.ui.detail;

import android.content.Intent;
import android.graphics.Color;

import androidx.activity.OnBackPressedCallback;
import androidx.core.view.WindowCompat;
import androidx.databinding.DataBindingUtil;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseActivity;
import com.kenz.almuslim.databinding.ActivityDetailBinding;

public class DetailActivity extends BaseActivity<DetailView, DetailPresenter> {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_detail;
    }

    @Override
    protected void beforeOnCreate() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    protected void onStarting() {
        ActivityDetailBinding binding = DataBindingUtil.setContentView(this, getLayoutId());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                presenter.onBackPressed();
            }
        });

        presenter.initView(this, binding);
    }

    @Override
    protected void onDestroyed() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected DetailPresenter initPresenter() {
        return new DetailPresenter();
    }
}
