package com.kenz.almuslim.ui.quotes.saved;

import com.kenz.almuslim.data.model.ModelQuote;

import java.util.ArrayList;

public interface SavedQuotesView {
    void showSavedQuotes(ArrayList<ModelQuote> quotesList);
    void showNoSavedQuotesView();
    void showToast(String message);
}
