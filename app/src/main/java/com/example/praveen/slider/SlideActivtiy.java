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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

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

public class SlideActivtiy extends AppCompatActivity implements SlideActivityAdapter.ViewHolder.ClickListener, UpdateTaskCompleteListener {

    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;
    private File fileSlideshow;
    SlideShowList slideList;
    private SlideActivityAdapter mAdapter;
    ViewMode viewMode;
    private Toolbar toolbar;
    private  CheckBox checkbox;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);
        slideList = new SlideShowList();
        viewMode = new ViewMode();
        initUi();
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
    }

    private void setUpToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(7);
        }
        setSupportActionBar(toolbar);
    }

    private void setRecyclerView() {
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView)findViewById(R.id.slideList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SlideActivityAdapter(getApplicationContext(), slideList, viewMode, this);
        mRecyclerView.setAdapter(mAdapter);
    }



    private void setFab() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewMode.getViewMode() == ViewMode.SINGLE_SELECT) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), SlideShowCreator.class);
                    intent.putExtra("slideList",slideList);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (viewMode.getViewMode() == ViewMode.MULTI_SELECT) {
                    deleteSelectedItems();
                }
            }
        });
    }

    private void deleteSelectedItems() {
        List<SlideShowInfo> list = new ArrayList<>();
        for(int pos : mAdapter.getSelectedItems()) {
            list.add(slideList.getSlideShowInfoList().get(pos));
        }
        slideList.removeSlideShowInfo(list);
        UpdateFileTask updateFileTask = new UpdateFileTask(this, fileSlideshow, slideList.getSlideShowInfoList());
        updateFileTask.listener = this;
        updateFileTask.execute();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            setViewMode(ViewMode.EDIT_SELECT);
            return true;
        } else if (id == R.id.action_delete) {
            setViewMode(ViewMode.MULTI_SELECT);
            mAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_slide_activtiy, menu);
        MenuItem edit = menu.findItem(R.id.action_edit);
        MenuItem delete = menu.findItem(R.id.action_delete);

        switch (viewMode.getViewMode()) {
            case ViewMode.SINGLE_SELECT :
                edit.setVisible(true);
                delete.setVisible(true);
                break;

            case ViewMode.EDIT_SELECT:
            case ViewMode.MULTI_SELECT:
                edit.setVisible(false);
                delete.setVisible(false);
                break;
        }
        return true;
    }

    private void setViewMode(int mode) {
        viewMode.setViewMode(mode);
        invalidateOptionsMenu();

        switch (viewMode.getViewMode()) {
            case ViewMode.SINGLE_SELECT :
                toolbar.setTitle("Slider");
                fab.show();
                fab.setImageResource(R.drawable.ic_add_white_48dp);
                mAdapter.clearSelection();
                mAdapter.notifyDataSetChanged();
                break;
            case ViewMode.MULTI_SELECT:
                toolbar.setTitle("Delete");
                fab.show();
                fab.setImageResource(R.drawable.ic_delete_white_24dp);
                mAdapter.clearSelection();
                break;
            case ViewMode.EDIT_SELECT:
                toolbar.setTitle("Edit");
                fab.hide();
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        switch (viewMode.getViewMode()) {
            case ViewMode.SINGLE_SELECT :
                startSlideViewActivity(position);
                break;
            case ViewMode.MULTI_SELECT:
                mAdapter.toggleSelection(position);
                break;
            case ViewMode.EDIT_SELECT:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SlideShowCreator.class);
                intent.putExtra("slideList",slideList);
                intent.putExtra("Mode",ViewMode.EDIT_SELECT);
                intent.putExtra("position",position);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    private void startSlideViewActivity(int pos) {
        Intent intent = new Intent(this, SlideView.class);
        intent.putExtra("slideShowInfo",slideList.getSlideShowInfoList().get(pos));
        ImageView v = (ImageView)findViewById(R.id.slideShowImage);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,(View)v,"tImage" );
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @Override
    public void onUpdateComplete() {
        if (viewMode.getViewMode() != ViewMode.SINGLE_SELECT) {
            setViewMode(ViewMode.SINGLE_SELECT);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        if ((viewMode.getViewMode() == ViewMode.MULTI_SELECT) || (viewMode.getViewMode() == ViewMode.EDIT_SELECT)) {
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
            fileSlideshow=new File(getExternalFilesDir(null).getAbsolutePath()+"/EnhancedSlideshowData.ser");
            if (fileSlideshow.exists()) {
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(fileSlideshow));
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
            slideList.setSlideShowInfoList(list);
            mAdapter.notifyDataSetChanged();
        }
    }
}
