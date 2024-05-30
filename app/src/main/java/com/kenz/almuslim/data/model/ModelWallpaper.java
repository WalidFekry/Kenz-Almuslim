package com.kenz.almuslim.data.model;


import android.graphics.Bitmap;


public class ModelWallpaper {
    public int wall_id;
    public int cat_id;
    public String wallpaper_title;
    public String wallpaper_image_thumb;
    public String wallpaper_image_detail;
    public String wallpaper_image_original;
    public String wallpaper_tags;
    public String wallpaper_type;
    public String wallpaper_premium;
    public int view_type;


    public Long numLikes;
    public Long numViews;


    public Bitmap imageThumb;
    public Bitmap imageOriginal;

    public ModelWallpaper(int wall_id,
                          int cat_id,
                          String wallpaper_title,
                          String wallpaper_image_thumb,
                          String wallpaper_image_detail,
                          String wallpaper_image_original,
                          String wallpaper_tags,
                          String wallpaper_type,
                          String wallpaper_premium,
                          int view_type,
                          Bitmap imageThumb,
                          Bitmap imageOriginal) {
        this.wall_id = wall_id;
        this.cat_id = cat_id;
        this.wallpaper_title = wallpaper_title;
        this.wallpaper_image_thumb = wallpaper_image_thumb;
        this.wallpaper_image_detail = wallpaper_image_detail;
        this.wallpaper_image_original = wallpaper_image_original;
        this.wallpaper_tags = wallpaper_tags;
        this.wallpaper_type = wallpaper_type;
        this.wallpaper_premium = wallpaper_premium;
        this.view_type = view_type;
        this.imageThumb = imageThumb;
        this.imageOriginal = imageOriginal;
    }

    public ModelWallpaper(int wall_id,
                          int cat_id,
                          String wallpaper_title,
                          String wallpaper_image_thumb,
                          String wallpaper_image_detail,
                          String wallpaper_image_original,
                          String wallpaper_tags,
                          String wallpaper_type,
                          String wallpaper_premium,
                          int view_type,
                          Long numLikes,
                          Long numViews) {
        this.wall_id = wall_id;
        this.cat_id = cat_id;
        this.wallpaper_title = wallpaper_title;
        this.wallpaper_image_thumb = wallpaper_image_thumb;
        this.wallpaper_image_detail = wallpaper_image_detail;
        this.wallpaper_image_original = wallpaper_image_original;
        this.wallpaper_tags = wallpaper_tags;
        this.wallpaper_type = wallpaper_type;
        this.wallpaper_premium = wallpaper_premium;
        this.view_type = view_type;
        this.numLikes = numLikes;
        this.numViews = numViews;
    }



    public ModelWallpaper(String wallpaper_premium, int view_type) {
        this.wallpaper_premium = wallpaper_premium;
        this.view_type = view_type;
    }

    public ModelWallpaper(int view_type) {
        this.view_type = view_type;
    }
}