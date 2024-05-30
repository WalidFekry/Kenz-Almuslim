package com.kenz.almuslim.data.local.model;


import android.graphics.Bitmap;

public class LocalWallpaper {

    public int wall_id;
    public int cat_id;
    public String wallpaper_title;
    public Bitmap wallpaper_image_thumb;
    public Bitmap wallpaper_image_original;
    public String wallpaper_image_detail;
    public String wallpaper_tags;
    public String wallpaper_type;
    public String wallpaper_premium;
    public int view_type;


    public LocalWallpaper() {
    }

    public LocalWallpaper(int wall_id,
                          int cat_id,
                          String wallpaper_title,
                          Bitmap wallpaper_image_thumb,
                          Bitmap wallpaper_image_original,
                          String wallpaper_image_detail,
                          String wallpaper_tags,
                          String wallpaper_type,
                          String wallpaper_premium,
                          int view_type) {
        this.wall_id = wall_id;
        this.cat_id = cat_id;
        this.wallpaper_title = wallpaper_title;
        this.wallpaper_image_thumb = wallpaper_image_thumb;
        this.wallpaper_image_original = wallpaper_image_original;
        this.wallpaper_image_detail = wallpaper_image_detail;
        this.wallpaper_tags = wallpaper_tags;
        this.wallpaper_type = wallpaper_type;
        this.wallpaper_premium = wallpaper_premium;
        this.view_type = view_type;
    }

    public int getWall_id() {
        return wall_id;
    }

    public void setWall_id(int wall_id) {
        this.wall_id = wall_id;
    }

    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    public String getWallpaper_title() {
        return wallpaper_title;
    }

    public void setWallpaper_title(String wallpaper_title) {
        this.wallpaper_title = wallpaper_title;
    }

    public Bitmap getWallpaper_image_thumb() {
        return wallpaper_image_thumb;
    }

    public void setWallpaper_image_thumb(Bitmap wallpaper_image_thumb) {
        this.wallpaper_image_thumb = wallpaper_image_thumb;
    }

    public Bitmap getWallpaper_image_original() {
        return wallpaper_image_original;
    }

    public void setWallpaper_image_original(Bitmap wallpaper_image_original) {
        this.wallpaper_image_original = wallpaper_image_original;
    }

    public String getWallpaper_image_detail() {
        return wallpaper_image_detail;
    }

    public void setWallpaper_image_detail(String wallpaper_image_detail) {
        this.wallpaper_image_detail = wallpaper_image_detail;
    }

    public String getWallpaper_tags() {
        return wallpaper_tags;
    }

    public void setWallpaper_tags(String wallpaper_tags) {
        this.wallpaper_tags = wallpaper_tags;
    }

    public String getWallpaper_type() {
        return wallpaper_type;
    }

    public void setWallpaper_type(String wallpaper_type) {
        this.wallpaper_type = wallpaper_type;
    }

    public String getWallpaper_premium() {
        return wallpaper_premium;
    }

    public void setWallpaper_premium(String wallpaper_premium) {
        this.wallpaper_premium = wallpaper_premium;
    }

    public int getView_type() {
        return view_type;
    }

    public void setView_type(int view_type) {
        this.view_type = view_type;
    }
}
