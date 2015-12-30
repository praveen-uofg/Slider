package com.example.praveen.slider.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by praveen on 12/26/2015.
 */
public class SlideShowList implements Serializable {
    public List<SlideShowInfo> getSlideShowInfoList() {
        if (mSlideShowInfoList == null) {
            mSlideShowInfoList = new ArrayList<>();
        }
        return mSlideShowInfoList;
    }

    public void setSlideShowInfoList(List<SlideShowInfo> slideShowInfoList) {
        this.mSlideShowInfoList = slideShowInfoList;
    }

    public void addSlideShowInfo(List<SlideShowInfo> list) {
        mSlideShowInfoList.clear();
        mSlideShowInfoList.addAll(list);
    }

    public void removeSlideShowInfo(List<SlideShowInfo> list) {
        mSlideShowInfoList.removeAll(list);
    }

    private List<SlideShowInfo> mSlideShowInfoList;
}
