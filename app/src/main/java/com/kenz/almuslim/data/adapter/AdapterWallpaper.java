package com.kenz.almuslim.data.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;


import com.kenz.almuslim.R;
import com.kenz.almuslim.utils.CommonUtils;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.DatabaseHandler;
import com.kenz.almuslim.ui.detail.DetailActivity;


public class AdapterWallpaper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_PROGRESS = 0;
    public static final int VIEW_ITEM = 1;
    public static final int VIEW_NATIVE = 2;
    private final DatabaseHandler databaseHandler;
    private final Activity activity;
    public ArrayList<ModelWallpaper> arrayList;
    private final ArrayList<ModelWallpaper> arrayListSearch;
    private int click_position;




    public AdapterWallpaper(Activity activity, ArrayList<ModelWallpaper> arrayList) {
        this.arrayList = arrayList;
        this.activity = activity;
        arrayListSearch = new ArrayList<>();
        arrayListSearch.addAll(arrayList);
        databaseHandler = new DatabaseHandler(activity);
    }


    static class OriginalViewHolder extends RecyclerView.ViewHolder {
        ImageView image, image_premium;
        ImageView gif_tag;
        LikeButton likeButton;
        LottieAnimationView progressBar;

        OriginalViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            image_premium = itemView.findViewById(R.id.image_premium);
            likeButton = itemView.findViewById(R.id.likeButton);
            progressBar = itemView.findViewById(R.id.progressBar);
            gif_tag = itemView.findViewById(R.id.gif_tag);
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        LottieAnimationView progressBar;

        ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    public static class NativeExpressAdViewHolder extends RecyclerView.ViewHolder {

        NativeExpressAdViewHolder(View view) {
            super(view);
        }
    }

    public static class NativeFacebookViewHolder extends RecyclerView.ViewHolder {
        TextView native_ad_title;
        TextView native_ad_sponsored_label;
        LinearLayout ad_choices_container;
        TextView native_ad_social_context;
        TextView native_ad_body;
        Button native_ad_call_to_action;
        ConstraintLayout root_view;

        NativeFacebookViewHolder(View view) {
            super(view);
            root_view = view.findViewById(R.id.root_view);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if (viewType == VIEW_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_wallpaper, parent, false);
            viewHolder = new OriginalViewHolder(view);
        } else if (viewType == VIEW_NATIVE) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(Constant.AdsOptions.IDENTIFIER.equals(Constant.AdsOptions.FACEBOOK) ? R.layout.lsv_item_native_fb : R.layout.lsv_item_native_big, parent, false);
//            if (Constant.AdsOptions.IDENTIFIER.equals(Constant.AdsOptions.FACEBOOK)) {
//                viewHolder = new NativeFacebookViewHolder(view);
//            } else {
//                viewHolder = new NativeExpressAdViewHolder(view);
//            }
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_wallpaper, parent, false);
            viewHolder = new OriginalViewHolder(view);

        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_progressbar, parent, false);
            viewHolder = new ProgressViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holderPrent, final int position) {
        final ModelWallpaper modelWallpaper = arrayList.get(position);
        switch (getItemViewType(position)) {
            case VIEW_NATIVE:
            case VIEW_ITEM:
                final OriginalViewHolder holder = (OriginalViewHolder) holderPrent;
                holder.likeButton.setLiked(databaseHandler.isFav(arrayList.get(position).wall_id));
                holder.gif_tag.setVisibility(modelWallpaper.wallpaper_type.equals("normal") ? View.GONE : View.VISIBLE);
                holder.image_premium.setVisibility(modelWallpaper.wallpaper_premium.equals("premium") ? View.VISIBLE : View.GONE);
                holder.likeButton.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        addFavourite(position);
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        removeFavourite(position);
                    }
                });
                Log.d("ABENK : ", modelWallpaper.wallpaper_image_thumb);
                if (arrayList.get(position).imageThumb != null) {
//                    Picasso.get().load(CommonUtils.getImageUri(activity, arrayList.get(position)
//                            .imageThumb))
//                            .into(holder.image);
                    holder.image.setImageBitmap(arrayList.get(position).imageThumb);
                    holder.progressBar.setVisibility(View.GONE);
                    holder.gif_tag.setVisibility(View.GONE);
                } else {
                    Picasso.get()
                            .load(modelWallpaper.wallpaper_image_thumb)
                            .placeholder(R.drawable.placeholder)
                            .into(holder.image, new Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            Constant.arrayListDetail = new ArrayList<>();
                            int native_position = 0;
                            for (int i = position != 0 ? (position - 1) : position; i < arrayList.size(); i++) {
                                if (Constant.arrayListDetail.size() < Constant.VIEW_PAGER_LIMIT) {
                                    if (arrayList.get(i).view_type != VIEW_PROGRESS && arrayList.get(i).view_type != VIEW_NATIVE) {
                                        if (native_position == Constant.VIEW_PAGER_LIMIT / 2) {
                                            Constant.arrayListDetail.add(new ModelWallpaper("", VIEW_NATIVE));
                                        }
                                        Constant.arrayListDetail.add(arrayList.get(i));
                                        native_position++;
                                    }
                                }
                            }
                            Intent intent = new Intent(activity, DetailActivity.class);
                            intent.putExtra("POS", position != 0 ? 1 : 0);
                            activity.startActivity(intent);
                            activity.overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                        }

                });
                break;
//            case VIEW_NATIVE:
//                if (Constant.AdsOptions.IDENTIFIER.equals(Constant.AdsOptions.FACEBOOK)) {
//                    // NO-OP
//                } else {
//                    // NO-OP
//                }
//                break;
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return arrayList.get(position).view_type;
    }

    private void addFavourite(int position) {
        if (!CommonUtils.isNetworkConnected(activity))
            return;
        int wall_id = arrayList.get(position).wall_id;
        int cat_id = arrayList.get(position).cat_id;
        String wallpaper_title = arrayList.get(position).wallpaper_title;
        String wallpaper_image_thumb = arrayList.get(position).wallpaper_image_thumb;
        String wallpaper_image_detail = arrayList.get(position).wallpaper_image_detail;
        String wallpaper_image_original = arrayList.get(position).wallpaper_image_original;
        String wallpaper_tags = arrayList.get(position).wallpaper_tags;
        String wallpaper_type = arrayList.get(position).wallpaper_type;
        String wallpaper_premium = arrayList.get(position).wallpaper_premium;
        Long numLikes = arrayList.get(position).numLikes;
        Long numViews = arrayList.get(position).numViews;
        databaseHandler.AddtoFavorite(new ModelWallpaper(wall_id, cat_id, wallpaper_title, wallpaper_image_thumb, wallpaper_image_detail, wallpaper_image_original,
                wallpaper_tags, wallpaper_type, wallpaper_premium, AdapterWallpaper.VIEW_ITEM, numLikes, numViews));
    }

    private void removeFavourite(int position) {
        if (!CommonUtils.isNetworkConnected(activity))
            return;
        databaseHandler.RemoveFav(arrayList.get(position).wall_id);
    }

    public void insertData(ArrayList<ModelWallpaper> arrayList) {
        this.arrayList.addAll(arrayList);
        notifyItemRangeInserted(getItemCount(), arrayList.size());
        if (arrayList.size() >= Constant.WALLPAPER_LOADING_POSITION) {
            this.arrayList.add(new ModelWallpaper(VIEW_PROGRESS));
            notifyItemRangeInserted(getItemCount(), 1);
        }
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void removeLoading() {
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).view_type == VIEW_PROGRESS) {
                arrayList.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void onDestroy() {
        databaseHandler.close();
    }
}