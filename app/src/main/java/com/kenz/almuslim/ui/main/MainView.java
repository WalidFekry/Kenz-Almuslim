package com.kenz.almuslim.ui.main;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.kenz.almuslim.data.base.MvpView;

public interface MainView extends MvpView {
    void BottomNavigationCenter();

    void BottomNavigationHome();

    void BottomNavigationCategory();


    void onFilterChosen(String filter);

    void showHorizontal();

    void showVertical();

}
