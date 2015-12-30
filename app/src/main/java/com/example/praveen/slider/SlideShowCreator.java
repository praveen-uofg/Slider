package com.example.praveen.slider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.praveen.slider.Adapters.SlideShowAdapter;
import com.example.praveen.slider.Interface.ItemTouchHelperViewHolder;
import com.example.praveen.slider.Interface.UpdateTaskCompleteListener;
import com.example.praveen.slider.Utils.SlideShowInfo;
import com.example.praveen.slider.Utils.SlideShowList;
import com.example.praveen.slider.Utils.UpdateFileTask;
import com.example.praveen.slider.Utils.ViewMode;

import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Created by praveen on 12/27/2015.
 */
public class SlideShowCreator extends AppCompatActivity implements Picker.PickListener, UpdateTaskCompleteListener, SlideShowAdapter.ViewHolder.ItemClickListener {

    private RecyclerView mImageRecycler;
    private FloatingActionButton fab;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private SlideShowAdapter mSlideAdapter;
    private SlideShowList slideShowList;
    private List <SlideShowInfo> infoList;
    private SlideShowInfo slideShowInfo;
    private File fileSlideshow;
    private int mMode;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;

    private static final int MUSIC_ID =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show_creator);
        initBasicValues();

    }

    private void initBasicValues() {
        mMode = getIntent().getIntExtra("Mode",1);
        slideShowList = (SlideShowList) getIntent().getSerializableExtra("slideList");
        fileSlideshow=new File(getExternalFilesDir(null).getAbsolutePath()+"/EnhancedSlideshowData.ser");
        infoList = slideShowList.getSlideShowInfoList();

        if (mMode == ViewMode.EDIT_SELECT) {
            int pos = getIntent().getIntExtra("position",0);
            slideShowInfo = infoList.get(pos);
            infoList.remove(pos);
        } else {
            slideShowInfo = new SlideShowInfo();
        }
    }

    private void initUi() {
        setActionBar();
        setUpRecyclerView();
        setUpFab();
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initUi();
    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mMode == ViewMode.EDIT_SELECT) {
            toolbar.setTitle("Slide Editor");
        } else {
            toolbar.setTitle("Slide Creator");
        }
    }

    private void setUpFab() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (slideShowInfo.getName() == null) {
                    createEditDialogBox();
                } else {
                    new Picker.Builder(SlideShowCreator.this, SlideShowCreator.this, R.style.AppTheme_NoActionBar)
                            .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                            .setLimit(20)
                            .build()
                            .startActivity();
                }
            }
        });
    }

    private void setUpRecyclerView() {
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        mImageRecycler = (RecyclerView)findViewById(R.id.list);
        mImageRecycler.setLayoutManager(mStaggeredGridLayoutManager);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(SimpleItemTouchHelper);
        mItemTouchHelper.attachToRecyclerView(mImageRecycler);
        mSlideAdapter = new SlideShowAdapter(this, slideShowInfo, this);
        mImageRecycler.setAdapter(mSlideAdapter);
        }


    @Override
    public void onItemClick(View parentView, int adapterPosition) {
        zoomImageFromThumb(parentView, adapterPosition);
    }

    @Override
    public void onPickedSuccessfully(ArrayList<ImageEntry> images) {
        if (slideShowInfo.getImageList() == null) {
            slideShowInfo.setImageList(images);
            mSlideAdapter.notifyItemRangeInserted(0, images.size());
        } else {
            int posStart = slideShowInfo.getImageList().size() -1;
            slideShowInfo.addImageList(images);
            mSlideAdapter.notifyItemRangeInserted(posStart, images.size());
        }

    }
    @Override
    public void onCancel() {
        Toast.makeText(this,R.string.canceled,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_creator_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id ==R.id.addMusic) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(intent , "choose slide music"),MUSIC_ID);
        } else if (id == R.id.done) {
            saveSlideShow();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSlideShow() {
        if (slideShowInfo.getMusicPath() != null && slideShowInfo.getImageList().size()>0) {
            infoList.add(slideShowInfo);
            slideShowList.setSlideShowInfoList(infoList);
            UpdateFileTask updateFileTask = new UpdateFileTask(this, fileSlideshow, slideShowList.getSlideShowInfoList());
            updateFileTask.listener = this;
            updateFileTask.execute();
        } else {
            Toast.makeText(this,"Please add music or images",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MUSIC_ID) {
            String path = getRealPathFromURI(data.getData());
            slideShowInfo.setMusicPath(path);
        }
    }

    private String getRealPathFromURI( Uri contentUri) {
        String[] proj = { MediaStore.Audio.Media.DATA };
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void createEditDialogBox() {
        LayoutInflater layoutInflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.item_name, null);
        final EditText editText= (EditText) view.findViewById(R.id.slidingName);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setPositiveButton("Set Name" , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String name=editText.getText().toString().trim();
                if (name.length()>0){
                    slideShowInfo.setName(name);
                    new Picker.Builder(SlideShowCreator.this, SlideShowCreator.this, R.style.MIP_theme)
                            .setPickMode(Picker.PickMode.MULTIPLE_IMAGES)
                            .setLimit(20)
                            .build()
                            .startActivity();
                }else{
                    Toast.makeText(SlideShowCreator.this, "please Enter Slide Name", Toast.LENGTH_LONG).show();

                }
            }
        });

        builder.setNegativeButton("Cancel" , null);
        builder.show();
    }

    @Override
    public void onUpdateComplete() {
        finish();
    }


    ItemTouchHelper.Callback SimpleItemTouchHelper = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.DOWN|ItemTouchHelper.UP|
                    ItemTouchHelper.START|ItemTouchHelper.END;
            int swipeFlags = ItemTouchHelper.START|ItemTouchHelper.END;
            return makeMovementFlags(dragFlags,swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            Collections.swap(slideShowInfo.getImageList(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
            mSlideAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            slideShowInfo.getImageList().remove(viewHolder.getAdapterPosition());
            mSlideAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                ItemTouchHelperViewHolder holder = (ItemTouchHelperViewHolder)viewHolder;
                holder.onItemSelected();
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            ItemTouchHelperViewHolder holder = (ItemTouchHelperViewHolder)viewHolder;
            holder.onItemClear();
        }
    };


    private void zoomImageFromThumb(final View thumbView, int pos) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.

        final ImageView mContainer = (ImageView) findViewById(R.id.imageContainer);
        Glide.with(this)
                .load(slideShowInfo.getImageList().get(pos).path)
                .asBitmap()
                .into(mContainer);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.frameContainer)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }


        //thumbView.setAlpha(0f);
        mContainer.setVisibility(View.VISIBLE);

        mContainer.setPivotX(0f);
        mContainer.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mContainer, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(mContainer, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(mContainer, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(mContainer,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(mContainer, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(mContainer,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(mContainer,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(mContainer,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        mContainer.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        mContainer.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

}
