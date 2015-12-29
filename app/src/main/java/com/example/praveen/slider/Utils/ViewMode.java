package com.example.praveen.slider.Utils;

/**
 * Created by praveen on 12/26/2015.
 */
public class ViewMode {
    public final static  int SINGLE_SELECT = 1;
    public final static  int MULTI_SELECT = 2;
    public final static int EDIT_SELECT = 3;

    public int getViewMode() {
        return viewMode;
    }

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;
    }

    int viewMode =1;
}
