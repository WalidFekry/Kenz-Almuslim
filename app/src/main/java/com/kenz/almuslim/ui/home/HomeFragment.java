package com.kenz.almuslim.ui.home;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseFragment;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.ui.main.MainActivity;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends BaseFragment<HomeView, HomePresenter> implements MainActivity.OnNewFeatures {

    public static final String TAG = "HomeFragment";

    @Override
    protected void onStarting() {
        presenter.initView((AppCompatActivity) requireActivity(), getRootView());
    }

    @Override
    protected void onDestroyed() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected HomePresenter initPresenter() {
        return new HomePresenter();
    }

    @Override
    public void onDestroy() {
        presenter.onPresenterDestroy();
        super.onDestroy();
    }

    @Override
    public void onFilterNameChosen(@NotNull String filterName) {
        if (filterName.equals("OLDER")) {
            Constant.filter_value = "FILTER_OLDER";
        } else if (filterName.equals("LATEST")) {
            Constant.filter_value = "FILTER_LATEST";
        }
        presenter.clearAll();
    }

    @Override
    public void onShowHorizontal() {
        presenter.clearAll();
    }

    @Override
    public void onShowVertical() {
        presenter.clearAll();
    }
}
