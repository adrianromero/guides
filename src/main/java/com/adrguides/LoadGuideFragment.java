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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.model.Section;
import com.adrguides.utils.HTTPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
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

                ExecutorService exec = Executors.newFixedThreadPool(5);

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

                    FutureImage future = new FutureImage(context, urldoc, imagesize);
                    future.beginExecutor();
                    Guide guide = loadGuide(text.toString(), future);
                    future.endExecutor();

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

        private Guide loadGuide(String text, FutureImage future) throws JSONException {


            JSONObject data = new JSONObject(text);

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
                        section.setImage(future.loadImage(s.optString("image")));
                    }
                    sections[j] = section;
                }
                places[i] = p;
            }
            return guide;
        }

        private class FutureImage {

            private ExecutorService exec;
            private Context context;
            private URL baseurl;
            private int imagesize;

            public FutureImage(Context context, URL baseurl, int imagesize) {
                this.context = context;
                this.baseurl = baseurl;
                this.imagesize = imagesize;
            }

            public void beginExecutor() {
                exec = Executors.newFixedThreadPool(5);
            }

            public void endExecutor() {
                exec.shutdown();
                try {
                    if (!exec.awaitTermination(120, TimeUnit.SECONDS)) {
                        exec.shutdownNow();
                        if (!exec.awaitTermination(120, TimeUnit.SECONDS)) {
                            System.err.println("Pool did not terminate");
                        }
                    }

                } catch (InterruptedException ie) {
                    exec.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            public String loadImage(final String address) {
                if (address == null || address.equals("")) {
                    return null;
                } else {
                    final String name = "guide-" + UUID.randomUUID().toString() + ".png";
                    exec.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                loadImageTask(address, name);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return name;
                }
            }

            private String loadImageTask(String address, String name) throws IOException {

                InputStream in = null;
                OutputStream out = null;

                try {
                    // read bitmap from source.
                    in = HTTPUtils.openAddress(context, new URL(baseurl, address));
                    Bitmap bmp = BitmapFactory.decodeStream(in);

                    // resize if needed to save space
                    int originsize = Math.min(bmp.getHeight(), bmp.getWidth());
                    if (originsize > imagesize) {
                        float factor = imagesize  / originsize;
                        Log.d("com.adrguides.LoadGuideFragment", "factor --> " + factor);
                        Bitmap newbmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() * factor), (int) (bmp.getHeight() * factor), true);
                        bmp.recycle();
                        bmp = newbmp;
                    }

                    // store in local filesystem.
                    out =  context.openFileOutput(name, Context.MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                    bmp.recycle();
                    return name;
                } finally {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null){
                        in.close();
                    }
                }
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
