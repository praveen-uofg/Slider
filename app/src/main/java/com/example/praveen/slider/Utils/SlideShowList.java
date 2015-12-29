package com.example.praveen.slider.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by praveen on 12/26/2015.
 */
public class SlideShowList implements Serializable {
    public List<SlideShowInfo> getSlideShowInfoList() {
        if (slideShowInfoList == null) {
            slideShowInfoList = new ArrayList<>();
        }
        return slideShowInfoList;
    }

    public void setSlideShowInfoList(List<SlideShowInfo> slideShowInfoList) {
        this.slideShowInfoList = slideShowInfoList;
    }

    public void addSlideShowInfo(List<SlideShowInfo> list) {
        slideShowInfoList.clear();
        slideShowInfoList.addAll(list);
    }

    public void removeSlideShowInfo(List<SlideShowInfo> list) {
        slideShowInfoList.removeAll(list);
    }

    List<SlideShowInfo> slideShowInfoList;
}
