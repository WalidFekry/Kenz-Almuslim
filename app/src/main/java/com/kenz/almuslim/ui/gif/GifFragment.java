package com.kenz.almuslim.ui.gif;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseFragment;

public class GifFragment extends BaseFragment<GifView, GifPresenter> {
    @Override
    protected void onStarting() {
        presenter.initView(requireActivity(), getRootView());
    }

    @Override
    protected void onDestroyed() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected GifPresenter initPresenter() {
        return new GifPresenter();
    }
}
