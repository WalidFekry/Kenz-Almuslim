package com.kenz.almuslim.data.base;


import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;


/*
 *  ****************************************************************************
 *  * Created by : Sudipta K Paik on 26-Aug-17 at 4:02 PM.
 *  * Email : sudipta@w3engineers.com
 *  *
 *  * Responsibility: Abstract fragment that every other fragment in this application must implement.
 *  *
 *  * Last edited by : Sudipta on 02-11-17.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */
public abstract class BaseFragment<V extends MvpView, P extends BasePresenter<V>> extends Fragment implements MvpView, View.OnClickListener {
    protected P presenter;
    private int mDefaultValue = -1;
    private ViewDataBinding viewDataBinding;
    private View rootView;
    protected abstract void onStarting();
    protected abstract void onDestroyed();
    protected abstract int getLayoutId();
    protected int getMenuId() {
        return mDefaultValue;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getMenuId() > mDefaultValue) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (getMenuId() > mDefaultValue) {
            inflater.inflate(getMenuId(), menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        int layoutId = getLayoutId();
        if (layoutId <= mDefaultValue) { // if default or invalid layout id, then no possibility to create view
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        try {
            viewDataBinding = DataBindingUtil.inflate(inflater, layoutId, container, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (viewDataBinding == null) {
            rootView = inflater.inflate(layoutId, container, false);
        }


        return viewDataBinding == null ? rootView : viewDataBinding.getRoot();
    }


    @SuppressWarnings("unchecked")
    @CallSuper
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BaseViewModel<V, P> baseViewModel = ViewModelProviders.of(this).get(BaseViewModel.class);
        boolean isPresenterCreated = false;
        if (baseViewModel.getPresenter() == null) {
            baseViewModel.setPresenter(initPresenter());
            isPresenterCreated = true;
        }
        presenter = baseViewModel.getPresenter();
        presenter.attachLifecycle(getLifecycle());
        presenter.attachView((V) this);
        if (isPresenterCreated)
            presenter.onPresenterCreated();
        onStarting();
    }

    public View getRootView() {
        if (viewDataBinding != null) {
            return viewDataBinding.getRoot();
        } else {
            if (rootView != null) {
                return rootView;
            } else {
                return getView();
            }
        }
    }

    protected static void runCurrentActivity(Activity activity, Class nextClass) {
        activity.startActivity(new Intent(activity, nextClass));
    }


    @CallSuper
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onDestroyed();
        if (presenter != null) {
            presenter.detachLifecycle(getLifecycle());
            presenter.detachView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachLifecycle(getLifecycle());
            presenter.detachView();
        }
        onDestroyed();
    }

    @Override
    public void onClick(View view) {

    }
    public void replaceFragment(int layoutId, BaseFragment fragment, String name) {
        for (int i = 0; i < requireActivity().getSupportFragmentManager().getBackStackEntryCount(); ++i) {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(layoutId, fragment, name);
        fragmentTransaction.commit();
    }

    public void addFragment(int layoutId, BaseFragment fragment, String name) {
        for (int i = 0; i < requireActivity().getSupportFragmentManager().getBackStackEntryCount(); ++i) {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(requireActivity().getSupportFragmentManager().getFragments().get(requireActivity().getSupportFragmentManager().getBackStackEntryCount()));
        fragmentTransaction.add(layoutId, fragment, name);
        fragmentTransaction.addToBackStack(name);
        fragmentTransaction.commit();
    }

    protected BaseFragment mBaseCurrentFragment;

    protected void callFragment(int layoutId, BaseFragment baseFragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(layoutId, baseFragment, baseFragment.getClass().getName())
                .commit();

        setCurrentFragment(baseFragment);
    }

    protected void setCurrentFragment(BaseFragment baseFragment) {
        this.mBaseCurrentFragment = baseFragment;
    }

    protected BaseFragment getBaseCurrentFragment() {
        return mBaseCurrentFragment;
    }
    protected abstract P initPresenter();
}