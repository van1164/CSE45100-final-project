package com.van1164.photoviewer.dto;

import android.graphics.Bitmap;

public class PhotoResponseDto {
    Bitmap bitmap;
    String title;
    String text;
    String author;
    String createdDate;


    public PhotoResponseDto(Bitmap bitmap, String title, String text, String author, String createdDate) {
        this.bitmap = bitmap;
        this.title = title;
        this.text = text;
        this.author = author;
        this.createdDate = createdDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

}
