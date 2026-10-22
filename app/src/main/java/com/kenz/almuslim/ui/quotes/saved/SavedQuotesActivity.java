package com.kenz.almuslim.ui.quotes.saved;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseActivity;
import com.kenz.almuslim.data.model.ModelQuote;
import com.kenz.almuslim.ui.quotes.AdapterQuotes;

import java.util.ArrayList;

public class SavedQuotesActivity extends BaseActivity<SavedQuotesView, SavedQuotesPresenter> implements SavedQuotesView, AdapterQuotes.OnQuoteActionListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View layoutNoSaved;
    private ImageView btnBack;

    private AdapterQuotes adapterQuotes;
    private ArrayList<ModelQuote> savedList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_saved_quotes;
    }

    @Override
    protected SavedQuotesPresenter initPresenter() {
        return new SavedQuotesPresenter();
    }

    @Override
    protected void beforeOnCreate() {
    }

    @Override
    protected void onStarting() {
        presenter.init(this);

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        layoutNoSaved = findViewById(R.id.layout_no_saved);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterQuotes = new AdapterQuotes(this, savedList, true, this);
        recyclerView.setAdapter(adapterQuotes);

        swipeRefreshLayout.setOnRefreshListener(() -> presenter.loadSavedQuotes());

        presenter.loadSavedQuotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.loadSavedQuotes();
        }
    }

    @Override
    protected void onDestroyed() {
    }

    @Override
    public void showSavedQuotes(ArrayList<ModelQuote> quotesList) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        this.savedList = quotesList != null ? quotesList : new ArrayList<>();
        if (adapterQuotes != null) {
            adapterQuotes.arrayList = this.savedList;
            adapterQuotes.notifyDataSetChanged();
        }
        if (layoutNoSaved != null) {
            layoutNoSaved.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showNoSavedQuotesView() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (layoutNoSaved != null) {
            layoutNoSaved.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // --- Adapter Callbacks ---

    @Override
    public void onSaveToggle(ModelQuote quote, int position) {
        presenter.deleteSavedQuote(quote);
    }

    @Override
    public void onCopy(ModelQuote quote) {
        presenter.copyQuoteText(quote);
    }

    @Override
    public void onWhatsAppShare(ModelQuote quote) {
        presenter.shareWhatsApp(quote);
    }

    @Override
    public void onMessengerShare(ModelQuote quote) {
        presenter.shareMessenger(quote);
    }

    @Override
    public void onGeneralShare(ModelQuote quote) {
        presenter.shareGeneralText(quote);
    }

    @Override
    public void onSaveAsImage(ModelQuote quote) {
        presenter.saveQuoteAsImage(quote);
    }

    @Override
    public void onDelete(ModelQuote quote, int position) {
        presenter.deleteSavedQuote(quote);
    }
}
