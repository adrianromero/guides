//        Guides is an Android application that reads audioguides using Text-to-Speech services.
//        Copyright (C) 2013  Adrián Romero Corchado
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.adrguides;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ViewSwitcher;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.model.Section;
import com.adrguides.tts.TextToSpeechSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by adrian on 2/09/13.
 */
public class LoadGuideFragment extends Fragment {

    public static final String TAG = "LoadGuideFragment-Tag";

    private LoadedGuide loadGuideresult = null;
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

    public class GuideLoader extends AsyncTask<Object, Void, LoadedGuide> {

        @Override
        protected LoadedGuide doInBackground(Object... params) {
            Context context = (Context) params[0];
            String url = (String) params[1];

            LoadedGuide result = new LoadedGuide();

            if (context == null) {
                result.setStatus(-1);
                result.setException("Context not available.");

            } else {
                try {
                    JSONObject data = HTTPUtils.execGET(context, url);

                    Guide guide = new Guide();
                    guide.setTitle(data.getString("title"));

                    JSONArray chapters = data.getJSONArray("chapters");
                    Place[] places = new Place[chapters.length()];
                    guide.setPlaces(places);
                    for (int i = 0; i < chapters.length(); i++) {
                        JSONObject chapter = chapters.getJSONObject(i);
                        Place p = new Place();
                        p.setId(chapter.has("id") ? chapter.getString("id") : null);
                        p.setTitle(chapter.getString("title"));
                        p.setImage(loadImage(context, chapter));

                        JSONArray paragraphs = chapter.getJSONArray("paragraphs");
                        Section[] sections = new Section[paragraphs.length()];
                        p.setSections(sections);
                        for (int j = 0; j < paragraphs.length(); j++) {
                            sections[j] = new Section();
                            JSONObject s = paragraphs.optJSONObject(j);
                            if (s == null) {
                                sections[j].setText(paragraphs.getString(j));
                            } else {
                                sections[j].setImage(loadImage(context, s));
                                sections[j].setText(s.getString("text"));
                            }
                        }
                        places[i] = p;
                    }

                    result.setStatus(0);
                    result.setGuide(guide);
                } catch (IOException e) {
                    Log.d("com.adrguides.GuideFragment", null, e);
                    result.setStatus(-1);
                    result.setException(e.getMessage());
                } catch (JSONException e) { // Parsing JSON text
                    Log.d("com.adrguides.GuideFragment", null, e);
                    result.setStatus(-1);
                    result.setException(e.getMessage());
                }
            }
            return result;
        }

        private Bitmap loadImage(Context context, JSONObject json) throws JSONException, IOException {
            if (json.has("image_asset")) {
                return BitmapFactory.decodeStream(context.getAssets().open(json.getString("image_asset")));
            } else {
                return null;
            }
        }


        @Override
        protected void onPostExecute(LoadedGuide r) {
            Log.d("com.adrguides.GuideFragment", "publishFinishLoaded");
            loadGuideresult = r;
            publishLoadGuideResult();
        }
        @Override
        protected void onCancelled() {
        }
    }

    public static interface LoadGuideCallbacks {
        public void onFinishLoad(LoadedGuide result);
    }
}
