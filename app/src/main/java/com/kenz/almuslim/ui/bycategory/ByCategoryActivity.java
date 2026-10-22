package com.kenz.almuslim.ui.bycategory;

import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseActivity;
import com.kenz.almuslim.data.utils.SharedPref;
import com.kenz.almuslim.databinding.ActivityByCategoryBinding;
import com.kenz.almuslim.ui.main.MainPresenter;

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.appBarLayout.setPadding(
                    binding.appBarLayout.getPaddingLeft(),
                    systemBars.top,
                    binding.appBarLayout.getPaddingRight(),
                    binding.appBarLayout.getPaddingBottom()
            );
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.no_change, R.anim.slide_down);
            }
        });

        presenter.initView(this, binding);
    }

    @Override
    protected void onDestroyed() {
    }

    @Override
    protected ByCategoryPresenter initPresenter() {
        return new ByCategoryPresenter();
    }
}
