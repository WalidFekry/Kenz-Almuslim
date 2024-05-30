package com.kenz.almuslim.data.base;

import android.view.View;

public interface BaseRecyclerViewClickListener<T>{
    void onItemClick(View view, T item, int i);
}
