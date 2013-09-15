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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ViewSwitcher;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.model.Section;
import com.adrguides.tts.TextToSpeechSingleton;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    public void loadGuide(Context context, String url) {
        loadGuideresult = null;
        Log.d("com.adrguides.GuideFragment", "loadGuide: " + url);
        Log.d("com.adrguides.GuideFragment", "getActivity() == null: " + (getActivity() == null));
        new GuideLoader().execute(context, url);

    }

    public class GuideLoader extends AsyncTask<Object, Void, LoadedGuide> {

        @Override
        protected LoadedGuide doInBackground(Object... params) {
            final Context context = (Context) params[0];
            final String url = (String) params[1];

            LoadedGuide result = new LoadedGuide();

            if (context == null) {
                result.setStatus(-1);
                result.setException("Context not available.");

            } else {

                initBitmapStorage(context);

                ExecutorService exec = Executors.newFixedThreadPool(5);

                try {
                    JSONObject data = HTTPUtils.execGET(context, url);

                    Guide guide = new Guide();
                    guide.setTitle(data.getString("title"));
                    guide.setLanguage(data.optString("language", "en"));
                    guide.setCountry(data.optString("country", "US"));
                    guide.setVariant(data.optString("variant", ""));

                    JSONArray chapters = data.getJSONArray("chapters");
                    Place[] places = new Place[chapters.length()];
                    guide.setPlaces(places);
                    for (int i = 0; i < chapters.length(); i++) {
                        final JSONObject chapter = chapters.getJSONObject(i);
                        final Place p = new Place();
                        p.setId(chapter.has("id") ? chapter.getString("id") : null);
                        p.setTitle(chapter.getString("title"));
                        exec.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    p.setImage(loadImage(context, chapter));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        JSONArray paragraphs = chapter.getJSONArray("paragraphs");
                        Section[] sections = new Section[paragraphs.length()];
                        p.setSections(sections);
                        for (int j = 0; j < paragraphs.length(); j++) {
                            final Section section = new Section();
                            final JSONObject s = paragraphs.optJSONObject(j);
                            if (s == null) {
                                section.setText(paragraphs.getString(j));
                            } else {
                                section.setText(s.getString("text"));
                                exec.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                           section.setImage(loadImage(context, s));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                            sections[j] = section;
                        }
                        places[i] = p;
                    }

                    result.setStatus(0);
                    result.setGuide(guide);

                    shutdownAndAwaitTermination(exec);

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

        void shutdownAndAwaitTermination(ExecutorService pool) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        System.err.println("Pool did not terminate");
                    }
                }

            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        private String loadImage(Context context, JSONObject json) throws JSONException, IOException {
            if (json.has("image_asset")) {
                return saveBitmap(context, BitmapFactory.decodeStream(context.getAssets().open(json.getString("image_asset"))));
            } else if (json.has("image_url")) {
                URL url = new URL(json.getString("image_url"));

                URLConnection urlconn =  url.openConnection();
                urlconn.setReadTimeout(10000 /* milliseconds */);
                urlconn.setConnectTimeout(15000 /* milliseconds */);
                urlconn.setAllowUserInteraction(false);
                urlconn.setDoInput(true);
                urlconn.setDoOutput(false);
                if (urlconn instanceof HttpURLConnection) {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responsecode = connection.getResponseCode();
                    if (responsecode != HttpURLConnection.HTTP_OK) {
                        return null;
                    }
                }
                InputStream in = null;
                try {
                    in = urlconn.getInputStream();
                    return saveBitmap(context, BitmapFactory.decodeStream(in));
                } finally {
                    if (in != null){
                        in.close();
                    }
                }
            } else {
                return null;
            }
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

        private String saveBitmap(Context context, Bitmap bmp) {

            String name = "guide-" + UUID.randomUUID().toString() + ".png";
            OutputStream out = null;
            try {
                out =  context.openFileOutput(name, Context.MODE_PRIVATE);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                return name;
            } catch (IOException e) {
                return null;
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
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
