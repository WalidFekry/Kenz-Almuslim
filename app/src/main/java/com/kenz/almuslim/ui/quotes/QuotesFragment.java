package com.kenz.almuslim.ui.quotes;

import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BaseFragment;
import com.kenz.almuslim.data.model.ModelQuote;
import com.kenz.almuslim.data.widgets.ProgressLoader;

import java.util.ArrayList;

public class QuotesFragment extends BaseFragment<QuotesView, QuotesPresenter> implements QuotesView, AdapterQuotes.OnQuoteActionListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View layoutOfflineEmpty;
    private View btnRetry;

    private AdapterQuotes adapterQuotes;
    private ProgressLoader progressLoader;
    private ArrayList<ModelQuote> quotesList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_quotes;
    }

    @Override
    protected QuotesPresenter initPresenter() {
        return new QuotesPresenter();
    }

    @Override
    protected void onStarting() {
        View rootView = getRootView();
        progressLoader = new ProgressLoader(requireActivity());

        presenter.init(requireActivity());

        recyclerView = rootView.findViewById(R.id.recycler_view);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);
        layoutOfflineEmpty = rootView.findViewById(R.id.layout_offline_empty);
        btnRetry = rootView.findViewById(R.id.btn_retry);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapterQuotes = new AdapterQuotes(requireActivity(), quotesList, false, this);
        recyclerView.setAdapter(adapterQuotes);

        swipeRefreshLayout.setOnRefreshListener(() -> presenter.loadQuotes());

        btnRetry.setOnClickListener(v -> presenter.loadQuotes());


        presenter.loadQuotes();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.loadQuotes();
        }
    }

    @Override
    protected void onDestroyed() {
    }

    @Override
    public void showQuotes(ArrayList<ModelQuote> quotes) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (progressLoader != null) {
            progressLoader.stopLoader();
        }

        this.quotesList = quotes != null ? quotes : new ArrayList<>();
        if (adapterQuotes != null) {
            adapterQuotes.arrayList = this.quotesList;
            adapterQuotes.notifyDataSetChanged();
        }

        if (layoutOfflineEmpty != null) {
            layoutOfflineEmpty.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showEmptyOrOfflineView() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (progressLoader != null) {
            progressLoader.stopLoader();
        }

        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (layoutOfflineEmpty != null) {
            layoutOfflineEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showLoading(boolean show) {
        if (show && progressLoader != null && !swipeRefreshLayout.isRefreshing()) {
            progressLoader.show();
        } else if (!show && progressLoader != null) {
            progressLoader.stopLoader();
        }
    }

    @Override
    public void showToast(String message) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // --- Adapter Callbacks ---

    @Override
    public void onSaveToggle(ModelQuote quote, int position) {
        presenter.toggleSaveQuote(quote, position);
        if (adapterQuotes != null) {
            adapterQuotes.notifyItemChanged(position);
        }
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
        // Not used in main quotes fragment
    }
}
