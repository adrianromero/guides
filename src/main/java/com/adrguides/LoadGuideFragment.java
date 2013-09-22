//        Guidebook is an Android application that reads audioguides using Text-to-Speech services.
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.model.Section;
import com.adrguides.utils.HTTPUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

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
        listening = true;
        if (loadGuideresult != null) {
            publishLoadGuideResult();
        }
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

    public void loadGuide(Context context, String url, int imagesize) {
        loadGuideresult = null;
        Log.d("com.adrguides.GuideFragment", "loadGuide: " + url);
        new GuideLoader().execute(context, url, imagesize);

    }

    public class GuideLoader extends AsyncTask<Object, Void, LoadedGuide> {

        @Override
        protected LoadedGuide doInBackground(Object... params) {
            final Context context = (Context) params[0];
            final String address = (String) params[1];
            final int imagesize = (Integer) params[2];

            LoadedGuide result = new LoadedGuide();

            if (context == null) {
                result.setStatus(-1);
                result.setException("Context not available.");

            } else {

                initBitmapStorage(context);

                InputStream inguide = null;
                try {
                    final URL urldoc = new URL(address);

                    // Read Document
                    inguide = HTTPUtils.openAddress(context, urldoc);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inguide, "UTF-8"));
                    StringBuffer text = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        text.append(line).append('\n');
                    }

                    LoadGuide loadguide;
                    if (urldoc.getPath().endsWith(".json")) {
                        loadguide = new LoadGuideJSON(context, urldoc, imagesize);
                    } else {
                        loadguide = new LoadGuideHTML(context, urldoc, imagesize);
                    }

                    Guide guide = loadguide.load(text.toString());

                    sanitized(guide);

                    result.setStatus(0);
                    result.setGuide(guide);

                } catch (IOException e) {
                    Log.d("com.adrguides.GuideFragment", null, e);
                    result.setStatus(-1);
                    result.setException(e.getMessage());
                } catch (Exception e) { // Parsing JSON text
                    Log.d("com.adrguides.GuideFragment", null, e);
                    result.setStatus(-1);
                    result.setException(e.getMessage());
                } finally {
                    if (inguide != null) {
                        try {
                            inguide.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            return result;
        }

        private void initBitmapStorage(Context context) {

            Log.d("com.adrguides.LoadGuideFragment", "dir -> " + context.getFilesDir().getAbsolutePath());

            File[] bmps = context.getFilesDir().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.startsWith("guide-");
                }
            });
            for (File b: bmps) {
                b.delete();
            }
        }

        private void sanitized(Guide guide) throws Exception {
            if (guide.getTitle() == null || guide.getTitle().equals("")) {
                throw new Exception("Guidebook needs to have a title.");
            }
            if (guide.getLanguage() == null || guide.getLanguage().equals("")) {
                throw new Exception("Guidebook needs to have a language.");
            }
            if (guide.getPlaces().size() == 0) {
                throw new Exception("Guidebook needs to have at least one chapter.");
            }

            for (Place p : guide.getPlaces()) {
                if (p.getTitle() == null || p.getTitle().equals("")) {
                    throw new Exception("All chapters needs to have a title.");
                }
                if (p.getSections().size() == 0) {
                    throw new Exception("All chapters needs to have at least one paragraph.");
                }
                for (Section s : p.getSections()) {
                    if (s.getText() == null || s.getText().equals("")) {
                        throw new Exception("All paragraphs needs to have at leat one word.");
                    }
                }
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
