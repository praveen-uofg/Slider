package com.example.praveen.slider.Utils;

import net.yazeed44.imagepicker.model.ImageEntry;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by praveen on 12/26/2015.
 */
public class SlideShowInfo implements Serializable{

    String musicPath;
    String name;
    ArrayList<ImageEntry> imageList;

    public SlideShowInfo() {
        musicPath = null;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ImageEntry> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<ImageEntry> imageList) {
        this.imageList = imageList;
    }

    public void addImageList(ArrayList<ImageEntry> list) {
            imageList.addAll(list);
    }
}
