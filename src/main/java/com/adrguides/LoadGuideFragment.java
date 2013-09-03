package com.adrguides;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ViewSwitcher;

import com.adrguides.model.Guide;
import com.adrguides.tts.TextToSpeechSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by adrian on 2/09/13.
 */
public class LoadGuideFragment extends Fragment {

    public static final String TAG = "LoadGuideFragment-Tag";

    private JSONObject loadGuideresult = null;
    private boolean listening = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    public void startListening() {
        if (loadGuideresult != null) {
            publishLoadGuideResult();
        }
        listening = true;
    }
    public void stopListening() {
        listening = false;
    }

    private void publishLoadGuideResult() {
        if (listening) {
            LoadGuideCallbacks callback = (LoadGuideCallbacks) getActivity();
            if (callback != null) {
                callback.onFinishLoad(loadGuideresult);
                loadGuideresult = null;
            }
        }
    }

    public void loadGuide(Context context, String url) {
        loadGuideresult = null;
        Log.d("com.adrguides.GuideFragment", "loadGuide: " + url);
        Log.d("com.adrguides.GuideFragment", "getActivity() == null: " + (getActivity() == null));
        new GuideLoader().execute(context, url);

    }

    public class GuideLoader extends AsyncTask<Object, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object... params) {
            Context context = (Context) params[0];
            String url = (String) params[1];

            JSONObject result = new JSONObject();
            try {
                if (context == null) {
                    result.put("status", -1);
                    result.put("exception", "Context not available.");

                } else {
                    try {
                        JSONObject data = HTTPUtils.execGET(context, url);
                        result.put("status", 0);
                        result.put("data", data);
                    } catch (IOException e) {
                        Log.d("com.adrguides.GuideFragment", null, e);
                            result.put("status", -1);
                            result.put("exception", e.getMessage());

                    }
                }
            } catch (JSONException e) { // Never thrown.
            }
            return result;
        }
        @Override
        protected void onPostExecute(JSONObject r) {
            Log.d("com.adrguides.GuideFragment", "publishFinishLoaded");
            loadGuideresult = r;
            publishLoadGuideResult();
        }
        @Override
        protected void onCancelled() {
        }
    }

    public static interface LoadGuideCallbacks {
        public void onFinishLoad(JSONObject result);
    }
}
