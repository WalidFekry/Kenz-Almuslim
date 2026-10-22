package com.kenz.almuslim.ui.quotes;

import com.kenz.almuslim.data.model.ModelQuote;

import java.util.ArrayList;

public interface QuotesView {
    void showQuotes(ArrayList<ModelQuote> quotesList);
    void showEmptyOrOfflineView();
    void showLoading(boolean show);
    void showToast(String message);
}
