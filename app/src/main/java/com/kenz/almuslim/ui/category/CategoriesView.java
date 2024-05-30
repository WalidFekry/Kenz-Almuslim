package com.kenz.almuslim.ui.category;

import com.kenz.almuslim.data.base.MvpView;

public interface CategoriesView extends MvpView {
    void onItemClick(int cat_id, String cat_name);
}
