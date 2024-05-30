package com.kenz.almuslim.data.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.kenz.almuslim.R;

import com.kenz.almuslim.data.utils.DatabaseHandler;
import com.kenz.almuslim.utils.CommonUtils;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import uc.benkkstudio.bscarouselviewpager.BSCarouselViewPager;
import uc.benkkstudio.bscarouselviewpager.BSCarouselViewPagerAdapter;

import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.Constant;

import org.jetbrains.annotations.NotNull;

public class ImagePagerAdapter extends BSCarouselViewPagerAdapter {

    private final LayoutInflater inflater;
    private final DatabaseHandler databaseHandler;
    private final Activity activity;
    private ArrayList<ModelWallpaper> arrayList;

    public ImagePagerAdapter(@NotNull Activity activity, ArrayList<ModelWallpaper> arrayList) {
        super(arrayList);
        this.activity = activity;
        this.arrayList = arrayList;
        inflater = activity.getLayoutInflater();
        databaseHandler = new DatabaseHandler(activity);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View imageLayout = null;
        if (arrayList.get(position).view_type == AdapterWallpaper.VIEW_NATIVE) {
            if (Constant.AdsOptions.IDENTIFIER.equals(Constant.AdsOptions.FACEBOOK)) {






                List<View> clickableViews = new ArrayList<>();


                imageLayout = inflater.inflate(R.layout.view_pager_native, container, false);

            }
        } else {
            imageLayout = inflater.inflate(R.layout.view_pager_wallpaper, container, false);
            ImageView imageView = imageLayout.findViewById(R.id.image);

            LikeButton likeButton = imageLayout.findViewById(R.id.likeButton);
            likeButton.setLiked(databaseHandler.isFav(arrayList.get(position).wall_id));

            likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    addFavourite(position);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    removeFavourite(position);
                }
            });

            final LottieAnimationView progressBar = imageLayout.findViewById(R.id.progressBar);
            if (arrayList.get(position).imageOriginal != null) {
//                Picasso.get().load(CommonUtils.getImageUri(activity, arrayList.get(position).imageOriginal)).into(imageView);
                imageView.setImageBitmap(arrayList.get(position).imageOriginal);
                progressBar.setVisibility(View.GONE);
            } else {
                Picasso.get()
                        .load(arrayList.get(position).wallpaper_image_detail)
                        .placeholder(R.drawable.placeholder)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            }
        }

        imageLayout.setTag(BSCarouselViewPager.ENCHANTED_VIEWPAGER_POSITION + position);
        container.addView(imageLayout);
        return imageLayout;

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


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private OnClickItemClickListener onClickItemClickListener;

    public void setOnClickItemClickListener(OnClickItemClickListener onClickItemClickListener) {
        this.onClickItemClickListener = onClickItemClickListener;
    }

    public interface OnClickItemClickListener {
        void onLiked(LikeButton likeButton);

        void onUnLiked(LikeButton likeButton);
    }

}