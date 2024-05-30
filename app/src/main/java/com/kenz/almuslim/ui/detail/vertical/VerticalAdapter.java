package com.kenz.almuslim.ui.detail.vertical;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.benkkstudio.bsjson.BSJson;
import com.benkkstudio.bsjson.BSObject;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;
import com.fangxu.allangleexpandablebutton.ButtonData;
import com.fangxu.allangleexpandablebutton.ButtonEventListener;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.adapter.AdapterWallpaper;
import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.DatabaseHandler;
import com.kenz.almuslim.ui.detail.DetailActivity;
import com.kenz.almuslim.ui.zoom.ZoomActivity;
import com.kenz.almuslim.utils.CommonUtils;
import com.kenz.almuslim.utils.WallPaperCommonUtils;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.kenz.almuslim.ui.detail.DetailPresenter.METHOD_SHARE;

public class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.VerticalImageViewHolder> {

    public ArrayList<ModelWallpaper> arrayList = new ArrayList<>();

    public static final int VIEW_PROGRESS = 0;
    public static final int VIEW_ITEM = 1;
    public static final int VIEW_NATIVE = 2;
    private final DatabaseHandler databaseHandler;

    private OnPageItemSelected onPageItemSelected;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final AppCompatActivity activity;

    public void setArrayList(ArrayList<ModelWallpaper> arrayList) {
        this.arrayList = arrayList;
    }

    public void setOnPageItemSelected(OnPageItemSelected onPageItemSelected) {
        this.onPageItemSelected = onPageItemSelected;
    }

    public VerticalAdapter(AppCompatActivity activity) {
        databaseHandler = new DatabaseHandler(activity);
        this.activity = activity;
    }

    private boolean isDetails = false;

    public void setDetails(boolean details) {
        isDetails = details;
    }




    @NonNull
    @Override
    public VerticalImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isDetails) {
            return new VerticalImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_pager_item_wallpaper, parent, false));
        } else {
            return new VerticalImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_pager_wallpaper, parent, false));
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull VerticalImageViewHolder holder, int position) {
        if (onPageItemSelected != null) {
            onPageItemSelected.onItemSelected(position);
        }
        if (arrayList.get(position).imageOriginal != null) {
            Picasso.get()
                    .load(CommonUtils.getImageUri(activity, arrayList.get(position).imageOriginal))
                    .into(holder.imageView);
            holder.progressBar.setVisibility(View.GONE);
        } else {
            Picasso.get()
                    .load(arrayList.get(position).wallpaper_image_detail)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
        }

        if (arrayList.get(position).numLikes != null) {
            holder.numLikes.setText(String.valueOf(arrayList.get(position).numLikes));
        }

        if (arrayList.get(position).numViews != null) {
            holder.numViews.setText(String.valueOf(arrayList.get(position).numViews));
        }

        activity.runOnUiThread(() -> {
            final List<ButtonData> buttonDatas = new ArrayList<>();
            final List<ButtonData> otherShares = new ArrayList<>();
            int[] drawable = {
                    R.drawable.ic_detail_plus,
                    R.drawable.ic_detail_zoom,
                    R.drawable.ic_detail_download,
                    R.drawable.ic_detail_crop,
                    R.drawable.ic_detail_share,
            };
            int[] secondDrawables = {
                    R.drawable.ic_detail_share,
                    R.drawable.ic_messenger,
                    R.drawable.ic_whatsapp,
            };
            final List<ButtonData> setAsBackgroundButton = new ArrayList<>();
            int[] setAsBackground = {
                    R.drawable.ic_phone_background,
            };
            for (int value : setAsBackground) {
                ButtonData buttonData = ButtonData.buildIconButton(activity, value, 12);
                buttonData.setBackgroundColorId(activity, R.color.colorButtonDetail);
                setAsBackgroundButton.add(buttonData);
            }
            for (int value : drawable) {
                ButtonData buttonData = ButtonData.buildIconButton(activity, value, 12);
                buttonData.setBackgroundColorId(activity, R.color.colorButtonDetail);
                buttonDatas.add(buttonData);
            }
            for (int value : secondDrawables) {
                ButtonData buttonData = ButtonData.buildIconButton(activity, value, 12);
                buttonData.setBackgroundColorId(activity, R.color.colorButtonDetail);
                otherShares.add(buttonData);
            }
            holder.defaultButton.setButtonDatas(buttonDatas);
            holder.shareButton.setButtonDatas(otherShares);
            holder.setButton.setButtonDatas(setAsBackgroundButton);
            holder.defaultButton.setButtonEventListener(new ButtonEventListener() {
                @Override
                public void onButtonClicked(int index) {
                    switch (index) {
                        case 1:
                            Intent intent = new Intent(activity, ZoomActivity.class);
                            intent.putExtra("str_image", Constant.arrayListDetail.get(position).wallpaper_image_original);
                            activity.startActivity(intent);
                            break;
                        case 2:
                            WallPaperCommonUtils.wallpaperTask("DOWNLOAD", activity, position);
                            break;
                        case 3:
                            if (CommonUtils.isNetworkConnected(activity)) {
                                WallPaperCommonUtils.wallpaperTask(Constant.arrayListDetail
                                                .get(position).wallpaper_type.equals("gif") ? "SET_WALLPAPER_GIF" : "SET_WALLPAPER",
                                        activity, position);
                            } else {
                                Toast.makeText(activity, "لا يوجد اتصال بالإنترنت", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 4:
                            WallPaperCommonUtils.wallpaperTask(METHOD_SHARE, activity, position);
                            break;
                    }
                }

                @Override
                public void onExpand() {

                }

                @Override
                public void onCollapse() {

                }
            });

            holder.shareButton.setButtonEventListener(new ButtonEventListener() {
                @Override
                public void onButtonClicked(int index) {
                    switch (index) {
                        case 1:
                            WallPaperCommonUtils.shareImage("messenger", activity, position);
                            break;
                        case 2:
                            WallPaperCommonUtils.shareImage("whatsapp", activity, position);
                            break;
                    }
                }

                @Override
                public void onExpand() {

                }

                @Override
                public void onCollapse() {

                }
            });

            holder.setButton.setButtonEventListener(new ButtonEventListener() {
                @Override
                public void onButtonClicked(int index) {

                }

                @Override
                public void onExpand() {
                    WallPaperCommonUtils.shareImage("wallpaper", activity, position);
                }

                @Override
                public void onCollapse() {

                }
            });

            holder.likeButton.setLiked(databaseHandler.isFav(arrayList.get(position).wall_id));
            holder.likeButton.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    long likes = arrayList.get(position).numLikes;
                    likes++;
                    holder.numLikes.setText(String.valueOf(likes));
                    handler.post(() -> {
                        incrementLikes(position);
                        addFavourite(position);
                    });
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    long likes = arrayList.get(position).numLikes;
                    likes--;
                    holder.numLikes.setText(String.valueOf(likes));
                    handler.post(() -> {
                        decrementLikes(position);
                        removeFavourite(position);
                    });
                }
            });

            if (!isDetails) {
                    holder.item.setOnClickListener(v -> {
                        Constant.arrayListDetail = new ArrayList<>();
                        Constant.arrayListDetail.addAll(arrayList);
                        Intent intent = new Intent(activity, DetailActivity.class);
                        intent.putExtra("POS", position);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                    });

            }

            if (isDetails) {
                holder.toolBox.setVisibility(View.GONE);
            }

        });

    }

    public static final String TAG = "VerticalAdapter";

    private void incrementLikes(int position) {
        if (!CommonUtils.isNetworkConnected(activity))
            return;
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "likes_increment");
        bsObject.addProperty("id", Constant.arrayListDetail.get(position).wall_id);
        new BSJson.Builder(activity)
                .setServer(Constant.SERVER_URL)
                .setObject(bsObject.getProperty())
                .setMethod(BSJson.METHOD_POST)
                .setListener(new BSJsonV2Listener() {
                    @Override
                    public void onLoaded(String response) {
                        Log.d("VerticalAdapter", "onLoaded: => " + response);
                    }

                    @Override
                    public void onError(String error) {
                        Log.d(TAG, "onError: => " + error);
                    }
                }).load();
    }


    private void decrementLikes(int position) {
        if (!CommonUtils.isNetworkConnected(activity))
            return;
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "likes_decrement");
        bsObject.addProperty("id", Constant.arrayListDetail.get(position).wall_id);
        new BSJson.Builder(activity)
                .setServer(Constant.SERVER_URL)
                .setObject(bsObject.getProperty())
                .setMethod(BSJson.METHOD_POST)
                .setListener(new BSJsonV2Listener() {
                    @Override
                    public void onLoaded(String response) {
                        Log.d("VerticalAdapter", "onLoaded: => " + response);
                    }

                    @Override
                    public void onError(String error) {
                        Log.d(TAG, "onError: => " + error);
                    }
                }).load();
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
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
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


    public static class VerticalImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public LikeButton likeButton;
        public LottieAnimationView progressBar;

        public final AllAngleExpandableButton defaultButton;
        public final AllAngleExpandableButton shareButton;

        public final AllAngleExpandableButton setButton;

        public final AppCompatTextView numLikes;
        public final AppCompatTextView numViews;

        public final CardView item;

        public final LinearLayout toolBox;


        public VerticalImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            progressBar = itemView.findViewById(R.id.progressBar);
            likeButton = itemView.findViewById(R.id.likeButton);
            defaultButton = itemView.findViewById(R.id.button_expandable);
            shareButton = itemView.findViewById(R.id.other_share_expandable);
            setButton = itemView.findViewById(R.id.as_background_expandable);
            numLikes = itemView.findViewById(R.id.num_likes);
            numViews = itemView.findViewById(R.id.num_views);
            item = itemView.findViewById(R.id.wallpaper_item);
            toolBox = itemView.findViewById(R.id.toolbox);
        }
    }

    public interface OnPageItemSelected {
        void onItemSelected(int position);
    }

}
