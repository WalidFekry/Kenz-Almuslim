package com.kenz.almuslim.data.base;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;


@SuppressWarnings("unchecked")
public abstract class BaseActivity<V extends MvpView, P extends BasePresenter<V>> extends AppCompatActivity implements MvpView{
    protected P presenter;
    protected ViewDataBinding dataBinding;
    protected abstract int getLayoutId();
    protected abstract void beforeOnCreate();
    protected abstract void onStarting();
    protected abstract void onDestroyed();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        beforeOnCreate();
        super.onCreate(savedInstanceState);
        try {
            dataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        } catch (Exception e) {
            if (dataBinding == null) {
                setContentView(getLayoutId());
            }
        }
        BaseViewModel<V, P> baseViewModel = ViewModelProviders.of(this).get(BaseViewModel.class);
        boolean isPresenterCreated = false;
        if (baseViewModel.getPresenter() == null) {
            baseViewModel.setPresenter(initPresenter());
            isPresenterCreated = true;
        }
        presenter = baseViewModel.getPresenter();
        presenter.attachLifecycle(getLifecycle());
        presenter.attachView((V) this);
        if (isPresenterCreated) {
            presenter.onPresenterCreated();
        }
        onStarting();
    }

    protected static void runCurrentActivity(Activity activity, Class nextClass) {
        activity.startActivity(new Intent(activity, nextClass));
    }

    @Override
    protected void onDestroy() {
        onDestroyed();
        super.onDestroy();
        presenter.detachLifecycle(getLifecycle());
        presenter.detachView();
    }

    protected abstract P initPresenter();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


}