package com.kenz.almuslim.ui.favourite;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseFragment;


public class FavouriteFragment extends BaseFragment<FavouriteView, FavouritePresenter> {
    @Override
    protected void onStarting() {
        presenter.initView(requireActivity(), getRootView());
    }

    @Override
    protected void onDestroyed() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_favourite;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected FavouritePresenter initPresenter() {
        return new FavouritePresenter();
    }
}
