package com.example.praveen.slider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.praveen.slider.Adapters.SlideActivityAdapter;
import com.example.praveen.slider.Interface.UpdateTaskCompleteListener;
import com.example.praveen.slider.Utils.SlideShowInfo;
import com.example.praveen.slider.Utils.SlideShowList;
import com.example.praveen.slider.Utils.UpdateFileTask;
import com.example.praveen.slider.Utils.ViewMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by praveen on 12/26/2015.
 */
public class SlideActivtiy extends AppCompatActivity implements SlideActivityAdapter.ViewHolder.ClickListener, UpdateTaskCompleteListener {

    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;
    private File mFileSlideshow;
    private SlideShowList mSlideList;
    private SlideActivityAdapter mAdapter;
    private ViewMode mViewMode;
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private TextView mEmptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);
        mSlideList = new SlideShowList();
        mViewMode = new ViewMode();
        startIntroActivity();


    }

    public void startIntroActivity() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(SlideActivtiy.this, IntroActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

    }
    @Override
    protected void onStart() {
        super.onStart();
        initUi();
        setViewMode(ViewMode.SINGLE_SELECT);
        new LoadSlideshowTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViewMode(ViewMode.SINGLE_SELECT);

            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
    }

    private void initUi() {
        setUpToolbar();
        setFab();
        setRecyclerView();
        mEmptyText = (TextView)findViewById(R.id.emptyView);
    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(7);
        }
        setSupportActionBar(mToolbar);
    }

    private void setRecyclerView() {
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView)findViewById(R.id.slideList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SlideActivityAdapter(getApplicationContext(), mSlideList, mViewMode, this);
        mRecyclerView.setAdapter(mAdapter);
    }



    private void setFab() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewMode.getViewMode() == ViewMode.SINGLE_SELECT) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), SlideShowCreator.class);
                    intent.putExtra("slideList", mSlideList);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (mViewMode.getViewMode() == ViewMode.MULTI_SELECT) {
                    deleteSelectedItems();
                }
            }
        });
    }

    private void deleteSelectedItems() {
        List<SlideShowInfo> list = new ArrayList<>();
        for(int pos : mAdapter.getSelectedItems()) {
            list.add(mSlideList.getSlideShowInfoList().get(pos));
        }
        mSlideList.removeSlideShowInfo(list);
        UpdateFileTask updateFileTask = new UpdateFileTask(this, mFileSlideshow, mSlideList.getSlideShowInfoList());
        updateFileTask.listener = this;
        updateFileTask.execute();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_changeView) {
            toggle(item);
            return true;
        } else if (id == R.id.action_edit) {
            toggle(item);
            return true;
        } else if (id == R.id.action_delete) {
            setViewMode(ViewMode.MULTI_SELECT);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggle(MenuItem item) {
        if (mLayoutManager.getSpanCount() != 1) {
            item.setIcon(R.drawable.ic_view_comfy_white_24dp);
            mLayoutManager.setSpanCount(1);
            mAdapter.notifyDataSetChanged();
        } else {
            item.setIcon(R.drawable.ic_view_list_white_24dp);
            mLayoutManager.setSpanCount(2);
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_slide_activtiy, menu);
        MenuItem viewToggle = menu.findItem(R.id.action_changeView);
        MenuItem edit = menu.findItem(R.id.action_edit);
        MenuItem delete = menu.findItem(R.id.action_delete);
        if (mAdapter.getItemCount() ==0) {
               viewToggle.setVisible(false);
               edit.setVisible(false);
               delete.setVisible(false);
        } else {
            switch (mViewMode.getViewMode()) {
                case ViewMode.SINGLE_SELECT:
                    viewToggle.setVisible(true);
                    edit.setVisible(true);
                    delete.setVisible(true);
                    break;

                case ViewMode.EDIT_SELECT:
                case ViewMode.MULTI_SELECT:
                    viewToggle.setVisible(true);
                    edit.setVisible(false);
                    delete.setVisible(false);
                    break;
            }
        }
        return true;
    }

    private void setViewMode(int mode) {
        mViewMode.setViewMode(mode);
        invalidateOptionsMenu();

        switch (mViewMode.getViewMode()) {
            case ViewMode.SINGLE_SELECT :
                mToolbar.setTitle("Slider");
                mFab.show();
                mFab.setImageResource(R.drawable.ic_add_white_48dp);
                mAdapter.clearSelection();
                mAdapter.notifyDataSetChanged();
                break;
            case ViewMode.MULTI_SELECT:
                mToolbar.setTitle("Delete");
                mFab.show();
                mFab.setImageResource(R.drawable.ic_delete_white_24dp);
                mAdapter.clearSelection();
                break;
            case ViewMode.EDIT_SELECT:
                mToolbar.setTitle("Edit");
                mFab.hide();
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        switch (mViewMode.getViewMode()) {
            case ViewMode.SINGLE_SELECT :
                startSlideViewActivity(position);
                break;
            case ViewMode.MULTI_SELECT:
                mAdapter.toggleSelection(position);
                break;
            case ViewMode.EDIT_SELECT:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SlideShowCreator.class);
                intent.putExtra("slideList",mSlideList);
                intent.putExtra("Mode",ViewMode.EDIT_SELECT);
                intent.putExtra("position",position);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    private void startSlideViewActivity(int pos) {
        Intent intent = new Intent(this, SlideView.class);
        intent.putExtra("slideShowInfo", mSlideList.getSlideShowInfoList().get(pos));
        ImageView v = (ImageView)findViewById(R.id.slideShowImage);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,v,"tImage" );
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @Override
    public void onUpdateComplete() {
        if (mViewMode.getViewMode() != ViewMode.SINGLE_SELECT) {
            setViewMode(ViewMode.SINGLE_SELECT);
            if (mAdapter.getItemCount() == 0) {
                mEmptyText.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.INVISIBLE);
            } else {
                mEmptyText.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if ((mViewMode.getViewMode() == ViewMode.MULTI_SELECT) || (mViewMode.getViewMode() == ViewMode.EDIT_SELECT)) {
            setViewMode(ViewMode.SINGLE_SELECT);
        } else {
            super.onBackPressed();
        }
    }

    class LoadSlideshowTask extends AsyncTask<Void,Void,List> {

        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SlideActivtiy.this);
            dialog.setMessage("Saving...Please Wait");
            dialog.setCancelable(false);
            dialog.show();
        }
        @Override
        protected List doInBackground(Void...Params) {
            List <SlideShowInfo> slideShowList = null;
            mFileSlideshow =new File(getExternalFilesDir(null).getAbsolutePath()+"/EnhancedSlideshowData.ser");
            if (mFileSlideshow.exists()) {
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(mFileSlideshow));
                    slideShowList = (List<SlideShowInfo>) objectInputStream.readObject();
                    objectInputStream.close();
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SlideActivtiy.this, e.getMessage() ,Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
            return slideShowList;
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            mSlideList.setSlideShowInfoList(list);
            if (mAdapter.getItemCount() == 0) {
                mEmptyText.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.INVISIBLE);
            } else {
                mEmptyText.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
            }
            invalidateOptionsMenu();
        }
    }
}
