package com.kenz.almuslim.ui.main;

public interface MainView {
    void BottomNavigationCenter();

    void BottomNavigationHome();

    void BottomNavigationCategory();


    void onFilterChosen(String filter);

    void showHorizontal();

    void showVertical();

}
