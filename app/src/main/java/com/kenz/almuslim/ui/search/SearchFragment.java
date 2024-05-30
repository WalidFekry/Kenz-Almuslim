package com.kenz.almuslim.ui.search;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseFragment;

public class SearchFragment extends BaseFragment<SearchView, SearchPresenter> {
    @Override
    protected void onStarting() {
        presenter.initView(requireActivity(), getRootView());
    }

    @Override
    protected void onDestroyed() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected SearchPresenter initPresenter() {
        return new SearchPresenter();
    }
}
