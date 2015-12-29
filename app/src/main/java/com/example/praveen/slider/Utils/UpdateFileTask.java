package com.example.praveen.slider.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.praveen.slider.Interface.UpdateTaskCompleteListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by praveen on 12/26/2015.
 */
public class UpdateFileTask extends AsyncTask<Void, Void, Void> {

    ProgressDialog mDialog;
    private Context mContext;
    private File fileSlideShow;
    private List<SlideShowInfo> slideShowInfos;
    public UpdateTaskCompleteListener listener ;

    public UpdateFileTask(Context context, File file, List<SlideShowInfo> list) {
        mContext = context;
        fileSlideShow = file;
        slideShowInfos = list;
        listener = null;

    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage("Please Wait...");
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (!fileSlideShow.exists()) {
                fileSlideShow.createNewFile();
            }
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(new FileOutputStream(fileSlideShow));
            objectOutputStream.writeObject(slideShowInfos);
            objectOutputStream.flush();
            objectOutputStream.close();

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        listener.onUpdateComplete();
    }
}
