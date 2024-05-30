package com.kenz.almuslim.data.base;

import androidx.lifecycle.ViewModel;

/**
 * Created by Dell on 10/9/2017.
 */

public class BaseViewModel<V extends MvpView, P extends Presenter<V>> extends ViewModel {
    private P presenter;

    void setPresenter(P presenter) {
        if (this.presenter == null) {
            this.presenter = presenter;
        }
    }

    P getPresenter() {
        return this.presenter;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        presenter.onPresenterDestroy();
        presenter = null;
    }
}
