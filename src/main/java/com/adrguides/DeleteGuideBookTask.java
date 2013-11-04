package com.adrguides;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.adrguides.model.Guide;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

/**
 * Created by adrian on 4/11/13.
 */
public class DeleteGuideBookTask extends AsyncTask<GuideBookItem, Void, String> {
    private Context appcontext;

    public DeleteGuideBookTask(Context appcontext) {
        this.appcontext = appcontext;
    }

    protected String doInBackground(GuideBookItem... params) {

        GuideBookItem item = params[0];

        // Delete the folder that contains the saved guidebook
        File f = new File(item.getFolder());
        File[] bmps = f.listFiles();
        for (File b: bmps) {
            b.delete();
        }
        f.delete();

        return appcontext.getString(R.string.msg_guidebook_deleted, item.getTitle());
    }

    @Override
    protected void onPostExecute(String result) {
        Toast toast = Toast.makeText(appcontext, result, Toast.LENGTH_SHORT);
        toast.show();
    }
}
