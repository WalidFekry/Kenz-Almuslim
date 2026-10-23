package com.kenz.almuslim.ui.quotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.model.ModelQuote;

import java.util.ArrayList;

public class AdapterQuotes extends RecyclerView.Adapter<AdapterQuotes.QuoteViewHolder> {

    public interface OnQuoteActionListener {
        void onSaveToggle(ModelQuote quote, int position);
        void onCopy(ModelQuote quote);
        void onWhatsAppShare(ModelQuote quote);
        void onMessengerShare(ModelQuote quote);
        void onGeneralShare(ModelQuote quote);
        void onSaveAsImage(ModelQuote quote);
        void onDelete(ModelQuote quote, int position);
    }

    private final Context context;
    public ArrayList<ModelQuote> arrayList;
    private final boolean isSavedMode;
    private final OnQuoteActionListener listener;

    public AdapterQuotes(Context context, ArrayList<ModelQuote> arrayList, boolean isSavedMode, OnQuoteActionListener listener) {
        this.context = context;
        this.arrayList = arrayList != null ? arrayList : new ArrayList<>();
        this.isSavedMode = isSavedMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quote, parent, false);
        return new QuoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        ModelQuote quote = arrayList.get(position);

        holder.tvBadge.setText("رسالة " + quote.id + "#" );
        holder.tvDate.setText(quote.createdAt != null ? quote.createdAt : "");
        holder.tvText.setText(quote.title != null ? quote.title : "");

        updateSaveUI(holder, quote.isSaved);

        if (isSavedMode) {
            holder.actionDelete.setVisibility(View.VISIBLE);
        } else {
            holder.actionDelete.setVisibility(View.GONE);
        }

        // Clicks
        View.OnClickListener saveListener = v -> {
            if (listener != null) {
                listener.onSaveToggle(quote, holder.getAdapterPosition());
            }
        };
        holder.btnSaveTop.setOnClickListener(saveListener);
        holder.actionSave.setOnClickListener(saveListener);

        holder.actionCopy.setOnClickListener(v -> {
            if (listener != null) listener.onCopy(quote);
        });

        holder.actionWhatsApp.setOnClickListener(v -> {
            if (listener != null) listener.onWhatsAppShare(quote);
        });

        holder.actionMessenger.setOnClickListener(v -> {
            if (listener != null) listener.onMessengerShare(quote);
        });

        holder.actionShare.setOnClickListener(v -> {
            if (listener != null) listener.onGeneralShare(quote);
        });

        holder.actionSaveImage.setOnClickListener(v -> {
            if (listener != null) listener.onSaveAsImage(quote);
        });

        holder.actionDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(quote, holder.getAdapterPosition());
        });
    }

    public void updateSaveUI(QuoteViewHolder holder, boolean isSaved) {
        if (isSaved) {
            holder.btnSaveTop.setImageResource(R.drawable.ic_bookmark_on);
            holder.ivSaveIcon.setImageResource(R.drawable.ic_bookmark_on);
            holder.tvSaveText.setText("محفوظة");
        } else {
            holder.btnSaveTop.setImageResource(R.drawable.ic_bookmark_off);
            holder.ivSaveIcon.setImageResource(R.drawable.ic_bookmark_off);
            holder.tvSaveText.setText("حفظ");
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class QuoteViewHolder extends RecyclerView.ViewHolder {

        TextView tvBadge, tvDate, tvText;
        ImageView btnSaveTop, ivSaveIcon;
        TextView tvSaveText;
        LinearLayout actionSave, actionWhatsApp, actionMessenger, actionCopy, actionShare, actionSaveImage, actionDelete;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadge = itemView.findViewById(R.id.tv_quote_badge);
            tvDate = itemView.findViewById(R.id.tv_quote_date);
            tvText = itemView.findViewById(R.id.tv_quote_text);
            btnSaveTop = itemView.findViewById(R.id.btn_save);

            actionSave = itemView.findViewById(R.id.action_save);
            ivSaveIcon = itemView.findViewById(R.id.iv_action_save_icon);
            tvSaveText = itemView.findViewById(R.id.tv_action_save_text);

            actionWhatsApp = itemView.findViewById(R.id.action_whatsapp);
            actionMessenger = itemView.findViewById(R.id.action_messenger);
            actionCopy = itemView.findViewById(R.id.action_copy);
            actionShare = itemView.findViewById(R.id.action_share);
            actionSaveImage = itemView.findViewById(R.id.action_save_image);
            actionDelete = itemView.findViewById(R.id.action_delete);
        }
    }
}
