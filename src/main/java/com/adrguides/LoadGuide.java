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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    protected URL baseurl;

    private Context context;
    private int imagesize;

    private HashMap<String, String> images = new HashMap<String, String>();

    public LoadGuide(Context context, URL baseurl, int imagesize) {
        this.context = context;
        this.baseurl = baseurl;
        this.imagesize = imagesize;
    }

    protected abstract Guide load_imp(String address, String file) throws Exception;

    public final Guide load(String address, String file) throws Exception {
        beginExecutor();
        Guide guide = load_imp(address, file);
        endExecutor();
        return guide;
    }

    protected final String loadImage(final String address) {
        if (address == null || address.equals("")) {
            return null;
        } else {
            String s = images.get(address);
            if (s != null) {
                return s;
            } else {
                final String name = "guide-" + UUID.randomUUID().toString() + ".png";
                images.put(address, name);
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
