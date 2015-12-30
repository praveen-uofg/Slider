package com.example.praveen.slider;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.praveen.slider.Utils.SlideShowInfo;

import java.io.IOException;
/**
 * Created by praveen on 12/28/2015.
 */
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SlideView extends AppCompatActivity implements MediaPlayer.OnPreparedListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
//    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
//    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private ImageView mImageView;
    private static final String MEDIA_TIME="MEDIA_TIME";
    private static final String IMAGE_INDEX="IMAGE_INDEX";
    private static final String SLIDESHOW_INFO="SLIDESHOW_INFO";
    private static final int DURATION=5000;

    private SlideShowInfo mSlideShowInfo;
    private MediaPlayer mMedisPlayer;
    private int mMediaTime;
    private int mNextImage;
    private Handler mHandler;
    private PowerManager.WakeLock mWakeLock;
    int mHeight;
    int mWidth;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    /*private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_slide_view);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mImageView = (ImageView) findViewById(R.id.videoViewImage);
        mHeight = mImageView.getHeight();
        mWidth = mImageView.getWidth();


        if (savedInstanceState == null) {
            mSlideShowInfo = (SlideShowInfo) getIntent().getSerializableExtra("slideShowInfo");
            mMediaTime = 0;
            mNextImage = 0;
        } else {
            mMediaTime = savedInstanceState.getInt(MEDIA_TIME);
            mNextImage = savedInstanceState.getInt(IMAGE_INDEX);
            mSlideShowInfo = (SlideShowInfo) savedInstanceState.getSerializable(SLIDESHOW_INFO);
        }

        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        mOptions.inSampleSize = 2;
        mHandler = new Handler();
        setMediaPlayer();
        setWakeLock();


        // Set up the user interaction to manually show or hide the system UI.
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        /*findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);*/
    }

    private void setWakeLock() {
        final PowerManager pm =(PowerManager)getSystemService(POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,"wakeLock");
        mWakeLock.acquire();
    }

    private void setMediaPlayer() {
        if (mSlideShowInfo.getMusicPath() != null) {
            try {
                mMedisPlayer = new MediaPlayer();
                mMedisPlayer.setDataSource(mSlideShowInfo.getMusicPath());
                mMedisPlayer.prepareAsync();
                mMedisPlayer.setLooping(true);
                mMedisPlayer.setOnPreparedListener(this);
                mMedisPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(getApplicationContext(),"Error = "+what,Toast.LENGTH_LONG).show();
                        return false;
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

/*    @Override
    protected void onResume() {
        super.onResume();
        if (mMedisPlayer != null) {
            mMedisPlayer.start();
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.post(updateSlideshow);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMedisPlayer != null) {
            mMedisPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(updateSlideshow);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMedisPlayer != null) {
            mMedisPlayer.release();
        }
        mWakeLock.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMedisPlayer != null) {
            outState.putInt(MEDIA_TIME,mMedisPlayer.getCurrentPosition());
        }
        outState.putInt(IMAGE_INDEX,mNextImage-1);
        outState.putSerializable(SLIDESHOW_INFO,mSlideShowInfo);
    }

    private Runnable updateSlideshow =new Runnable() {
        @Override
        public void run() {
            if (mNextImage >= mSlideShowInfo.getImageList().size()) {
                if (mMedisPlayer != null && mMedisPlayer.isPlaying()) {
                    mMedisPlayer.reset();
                    mMedisPlayer.stop();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition();
                    } else {
                        supportFinishAfterTransition();
                    }
                }
            } else {
                String path = mSlideShowInfo.getImageList().get(mNextImage).path;
                new LoadImageTask().execute(path);
                ++mNextImage;

            }

        }
    };

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mMedisPlayer != null) {
            mMedisPlayer.start();
            mMedisPlayer.seekTo(mMediaTime);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqH, int reqW) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqH || width > reqW) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqH
                    && (halfWidth / inSampleSize) > reqW) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    class LoadImageTask extends AsyncTask<String , Object , Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = calculateInSampleSize(options, 720, 1280);

            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(params[0], options);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            BitmapDrawable next=new BitmapDrawable(bitmap);
            next.setGravity(Gravity.CENTER);
            Drawable previous=mImageView.getDrawable();

            if (previous instanceof TransitionDrawable){

                previous=((TransitionDrawable)previous).getDrawable(1);
            }

            if (previous==null){
                mImageView.setImageDrawable(next);
            }else{
                Drawable[] drawables={previous , next};
                TransitionDrawable transitionDrawable=new TransitionDrawable(drawables);
                transitionDrawable.setCrossFadeEnabled(true);
                mImageView.setImageDrawable(transitionDrawable);
                transitionDrawable.startTransition(1500);
            }
            mHandler.postDelayed(updateSlideshow,DURATION);
        }
    }
}
