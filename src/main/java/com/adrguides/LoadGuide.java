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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.adrguides.model.Guide;
import com.adrguides.utils.GuidesException;
import com.adrguides.utils.HTTPUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by adrian on 19/09/13.
 */
public abstract class LoadGuide {

    private ExecutorService exec;

    private Context context;
    private int imagesize;

    private HashMap<String, String> images = new HashMap<String, String>();

    public LoadGuide(Context context, int imagesize) {
        this.context = context;
        this.imagesize = imagesize;
    }

    protected abstract Guide load_imp(URL address, String file) throws Exception;

    public final Guide load(URL address) throws Exception {

        InputStream inguide = null;
        try {
            // Read Document
            inguide = HTTPUtils.openAddress(context, address);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inguide, "UTF-8"));
            StringBuffer text = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append('\n');
            }

            beginExecutor();
            Guide guide = load_imp(address, text.toString());
            endExecutor();
            return guide;
        } finally {
            if (inguide != null) {
                try {
                    inguide.close();
                } catch (IOException e) {
                }
            }
        }
    }

    protected final String loadImage(final URL baseurl, final String address) throws GuidesException {
        try {
            if (address == null || address.equals("")) {
                return null;
            } else {
                String s = images.get(address);
                if (s != null) {
                    return s;
                } else {
                    final File file = new File(context.getFilesDir(), "guide-" + UUID.randomUUID().toString() + ".png");
                    final String url = file.toURI().toURL().toString();
                    images.put(address, url);
                    exec.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                loadImageTask(baseurl, address, file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return url;
                }
            }
        } catch (IOException e) {
            throw new GuidesException(R.string.ex_imagenotfound, "Refered image has not been found.");
        }
    }

    private void beginExecutor() {
        exec = Executors.newFixedThreadPool(5);
    }

    private void endExecutor() {
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
        } finally {
            exec = null;
        }
    }


    private void loadImageTask(URL baseurl, String address, File file) throws IOException {

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
            out = new FileOutputStream(file); // context.openFileOutput(name, Context.MODE_PRIVATE);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            bmp.recycle();
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
