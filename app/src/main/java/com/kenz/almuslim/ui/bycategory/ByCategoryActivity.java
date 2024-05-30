package com.kenz.almuslim.ui.bycategory;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseActivity;
import com.kenz.almuslim.data.utils.SharedPref;
import com.kenz.almuslim.databinding.ActivityByCategoryBinding;
import com.kenz.almuslim.ui.main.MainPresenter;

import androidx.databinding.DataBindingUtil;


public class ByCategoryActivity extends BaseActivity<ByCategoryView, ByCategoryPresenter> {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_by_category;
    }

    @Override
    protected void beforeOnCreate() {
        if (SharedPref.getSharedPref(this).contains(MainPresenter.THEME_MODE) && SharedPref.getSharedPref(this).readBoolean(MainPresenter.THEME_MODE)) {
            setTheme(R.style.BStudioThemeDark);
        } else {
            setTheme(R.style.BStudioThemeLight);
        }
    }

    @Override
    protected void onStarting() {
        ActivityByCategoryBinding binding = DataBindingUtil.setContentView(this, getLayoutId());
        presenter.initView(this, binding);
    }

    @Override
    protected void onDestroyed() {
    }

    @Override
    public void onBackPressed(){
        finish();
        overridePendingTransition(R.anim.no_change, R.anim.slide_down);
    }

    @Override
    protected ByCategoryPresenter initPresenter() {
        return new ByCategoryPresenter();
    }
}
