package com.kenz.almuslim.data.model;


public class ModelCategories {
    public int id;
    public String category_name;
    public String category_image_thumb;
    public String category_image_original;
    public String total_wallpaper;
    public int viewType;

    public ModelCategories(int id, String category_name, String category_image_thumb, String category_image_original, String total_wallpaper, int viewType) {
        this.id = id;
        this.category_name = category_name;
        this.category_image_thumb = category_image_thumb;
        this.category_image_original = category_image_original;
        this.total_wallpaper = total_wallpaper;
        this.viewType = viewType;
    }

    public ModelCategories(int viewType) {
        this.viewType = viewType;
    }
}
