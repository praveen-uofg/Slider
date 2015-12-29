package com.example.praveen.slider;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.praveen.slider.Utils.SlideShowInfo;
import com.example.praveen.slider.Utils.SlideShowList;
import com.example.praveen.slider.Utils.ViewMode;

/**
 * Created by praveen on 12/26/2015.
 */
public class SlideActivityAdapter extends SelectableAdapter<SlideActivityAdapter.ViewHolder> {

    private SlideShowList slideList;
    private SlideShowInfo slideShowInfo;
    private Context mContext;
    ViewMode viewMode;
    ViewHolder.ClickListener mClickListener;

    public SlideActivityAdapter(Context context, SlideShowList slideShowList, ViewMode viewMode, ViewHolder.ClickListener clickListener) {
        mContext = context;
        slideList = slideShowList;
        this.viewMode = viewMode;
        mClickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.slide_row,parent, false);
        return new ViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        slideShowInfo = slideList.getSlideShowInfoList().get(position);
        holder.placeName.setText(slideShowInfo.getName());
        Glide.with(mContext)
                .load(slideShowInfo.getImageList().get(0).path)
                .asBitmap()
                .into(holder.slideShowImage);
        if (viewMode.getViewMode() == ViewMode.MULTI_SELECT) {
            holder.selectedView.setVisibility(isSelected(position)? View.VISIBLE : View.GONE);
            holder.playImage.setVisibility(View.GONE);
        } else if (viewMode.getViewMode() == ViewMode.SINGLE_SELECT){
            holder.selectedView.setVisibility( View.VISIBLE);
            holder.playImage.setVisibility(View.VISIBLE);
        } else if (viewMode.getViewMode() == ViewMode.EDIT_SELECT) {
            holder.selectedView.setVisibility( View.GONE);
            holder.playImage.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        if (slideList != null && slideList.getSlideShowInfoList() != null) {
            return slideList.getSlideShowInfoList().size();
        }
        return 0;
    }



    public static  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView placeName;
        public ImageView slideShowImage;
        public View selectedView;
        public ImageView playImage;
        ClickListener listener;
        private View parentView;


        public ViewHolder(View itemView, ClickListener clickListener) {
            super(itemView);
            placeName = (TextView) itemView.findViewById(R.id.placeName);
            slideShowImage = (ImageView) itemView.findViewById(R.id.slideShowImage);
            selectedView = itemView.findViewById(R.id.selected_overlay);
            playImage = (ImageView)itemView.findViewById(R.id.playImage);
            listener = clickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClick( getAdapterPosition());
            }
        }
        public interface ClickListener {
            public void onItemClick(int position);
        }
    }

}
