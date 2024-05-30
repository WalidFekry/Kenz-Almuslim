package com.kenz.almuslim.ui.detail;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseActivity;
import com.kenz.almuslim.databinding.ActivityDetailBinding;

import androidx.databinding.DataBindingUtil;


public class DetailActivity extends BaseActivity<DetailView, DetailPresenter> {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_detail;
    }

    @Override
    protected void beforeOnCreate() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    protected void onStarting() {
        ActivityDetailBinding binding = DataBindingUtil.setContentView(this, getLayoutId());
        presenter.initView(this, binding);
    }

    @Override
    protected void onDestroyed() {

    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
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
