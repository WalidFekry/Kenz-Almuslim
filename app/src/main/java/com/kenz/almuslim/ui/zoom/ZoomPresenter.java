package com.kenz.almuslim.ui.zoom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.widgets.ProgressLoader;
import com.kenz.almuslim.databinding.ActivityZoomBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import androidx.annotation.Nullable;


public class ZoomPresenter extends BasePresenter<ZoomView> {
    protected void initView(Context context, final ActivityZoomBinding mBinding, String str_image){
        ProgressLoader progressLoader = new ProgressLoader(context);
        progressLoader.show();
        Glide.with(context)
                .load(str_image)
                .placeholder(R.drawable.placeholder)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mBinding.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mBinding.zoomImage);
        progressLoader.stopLoader();
    }
}
