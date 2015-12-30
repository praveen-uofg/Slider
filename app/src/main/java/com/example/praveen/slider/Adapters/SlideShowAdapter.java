package com.example.praveen.slider.Adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.praveen.slider.Interface.ItemTouchHelperViewHolder;
import com.example.praveen.slider.R;
import com.example.praveen.slider.Utils.SlideShowInfo;

/**
 * Created by praveen on 12/27/2015.
 */
public class SlideShowAdapter extends RecyclerView.Adapter<SlideShowAdapter.ViewHolder> {
    private  static Context sContext;
    private SlideShowInfo slideShowInfo;
    private ViewHolder.ItemClickListener mListener;

    public SlideShowAdapter(Context context, SlideShowInfo slideShowInfo, ViewHolder.ItemClickListener listener) {
        sContext = context;
        this.slideShowInfo = slideShowInfo;
        if (slideShowInfo == null) {
            this.slideShowInfo = new SlideShowInfo();
        }
        mListener = listener;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row,parent,false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String path = slideShowInfo.getImageList().get(position).path;
        Glide.with(sContext)
                .load(path)
                .asBitmap()
                .into(holder.mSlideImage);
    }

    @Override
    public int getItemCount() {
        if (slideShowInfo != null && slideShowInfo.getImageList() != null) {
            return slideShowInfo.getImageList().size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, View.OnClickListener{

        public ImageView mSlideImage;
        public CardView mContainer;
        public View parentView;
        private ItemClickListener mListener;
        public ViewHolder(View itemView , ItemClickListener listener) {
            super(itemView);
            this.parentView =itemView;
            mSlideImage = (ImageView)itemView.findViewById(R.id.slideImage);
            mContainer = (CardView) itemView.findViewById(R.id.placeCard);
            parentView.setOnClickListener(this);
            mListener = listener;
        }

        @Override
        public void onItemSelected() {
            parentView.setBackgroundColor(sContext.getResources().getColor(android.R.color.background_dark));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mContainer.setElevation(20);
            }
        }

        @Override
        public void onItemClear() {
            parentView.setBackgroundColor(sContext.getResources().getColor(android.R.color.background_light));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mContainer.setElevation(5);

            }
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(parentView, getAdapterPosition());
            }
        }

        public interface ItemClickListener {
            void onItemClick(View parentView, int adapterPosition);
        }
    }
}
