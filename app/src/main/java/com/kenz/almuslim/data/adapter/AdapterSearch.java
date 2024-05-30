package com.kenz.almuslim.data.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.kenz.almuslim.R;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.DatabaseHandler;
import com.kenz.almuslim.ui.detail.DetailActivity;

public class AdapterSearch extends RecyclerView.Adapter<AdapterSearch.ViewHolder> {
    private Activity activity;
    private ArrayList<ModelWallpaper> arrayList;
    private ArrayList<ModelWallpaper> arrayListSearch;
    private DatabaseHandler databaseHandler;

    public AdapterSearch(Activity activity, ArrayList<ModelWallpaper> arrayList) {
        this.arrayList = arrayList;
        this.activity = activity;
        arrayListSearch = new ArrayList<>();
        arrayListSearch.addAll(arrayList);
        databaseHandler = new DatabaseHandler(activity);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, image_premium;
        ImageView gif_tag;
        LikeButton likeButton;
        LottieAnimationView progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            image_premium = itemView.findViewById(R.id.image_premium);
            likeButton = itemView.findViewById(R.id.likeButton);
            progressBar = itemView.findViewById(R.id.progressBar);
            gif_tag = itemView.findViewById(R.id.gif_tag);
        }

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_wallpaper, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterSearch.ViewHolder holder, final int position) {
        final ModelWallpaper modelWallpaper = arrayList.get(position);
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.arrayListDetail = new ArrayList<>();
                Constant.arrayListDetail.addAll(arrayList);
                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra("POS", position);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    private void addFavourite(int position) {
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
        databaseHandler.RemoveFav(arrayList.get(position).wall_id);
    }

    public void search(String charText) {
        arrayList.clear();
        if (charText.toLowerCase(Locale.getDefault()).length() == 0) {
            arrayList.addAll(arrayListSearch);
        } else {
            for (ModelWallpaper wp : arrayListSearch) {
                if (wp.wallpaper_tags.toLowerCase(Locale.getDefault()).contains(charText.toLowerCase(Locale.getDefault()))) {
                    arrayList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void onDestroy() {
        databaseHandler.close();
    }
}