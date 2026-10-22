package com.kenz.almuslim.data.model;

import com.google.gson.annotations.SerializedName;

public class ModelQuote {
    @SerializedName("id")
    public int id;

    @SerializedName("title")
    public String title;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("imageurl")
    public String imageUrl;

    public boolean isSaved;

    public ModelQuote() {
    }

    public ModelQuote(int id, String title, String createdAt, String imageUrl) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.isSaved = false;
    }

    public ModelQuote(int id, String title, String createdAt, String imageUrl, boolean isSaved) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.isSaved = isSaved;
    }
}
