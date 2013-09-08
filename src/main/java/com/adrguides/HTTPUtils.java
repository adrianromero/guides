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

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;

import org.json.JSONException;
import org.json.JSONObject;

public class HTTPUtils {


    public static JSONObject execGETMock(Context context, String address) throws IOException {

        Log.d("com.adrguides.HTTPUtils", "loading");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("mockguide.json"), "UTF-8"));

        StringBuffer jsontext = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            jsontext.append(line).append('\n');
        }

        reader.close();

        try {
            Log.i("com.adrguides.HTTPUtils", "result -> " + jsontext.toString());
            return new JSONObject(jsontext.toString());
        } catch (JSONException ex) {
            throw new IOException(MessageFormat.format("Parse exception: {0}.", ex.getMessage()));
        }
    }

    public static JSONObject execGET(Context context, String address) throws IOException {

        if (true) {
            return execGETMock(context, address);
        }

        BufferedReader readerin = null;

        try {
            URL url = new URL(address);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);
            connection.setRequestMethod("GET");
            connection.setAllowUserInteraction(false);
            connection.setDoInput(true);

            int responsecode = connection.getResponseCode();
            if (responsecode == HttpURLConnection.HTTP_OK) {
                StringBuilder text = new StringBuilder();

                readerin = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = readerin.readLine()) != null) {
                    text.append(line);
                    text.append(System.getProperty("line.separator"));
                }

                JSONObject result = new JSONObject(text.toString());

                if (result.has("exception")) {
                    throw new IOException(MessageFormat.format("Remote exception: {0}.", result.getString("exception")));
                } else {
                    return result;
                }
            } else {
                throw new IOException(MessageFormat.format("HTTP response error: {0}. {1}", Integer.toString(responsecode), connection.getResponseMessage()));
            }
        } catch (JSONException ex) {
            throw new IOException(MessageFormat.format("Parse exception: {0}.", ex.getMessage()));
        } finally {
            if (readerin != null) {
                readerin.close();
                readerin = null;
            }
        }
    }

    public static JSONObject execPOST(String address, JSONObject params) throws IOException {

        BufferedReader readerin = null;
        Writer writerout = null;

        try {
            URL url = new URL(address);
            String query = params.toString();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);
            connection.setRequestMethod("POST");
            connection.setAllowUserInteraction(false);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.addRequestProperty("Content-Type", "application/json,encoding=UTF-8");
            connection.addRequestProperty("Content-length", String.valueOf(query.length()));

            writerout = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writerout.write(query);
            writerout.flush();

            writerout.close();
            writerout = null;

            int responsecode = connection.getResponseCode();
            if (responsecode == HttpURLConnection.HTTP_OK) {
                StringBuilder text = new StringBuilder();

                readerin = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String line = null;
                while ((line = readerin.readLine()) != null) {
                    text.append(line);
                    text.append(System.getProperty("line.separator"));
                }

                JSONObject result = new JSONObject(text.toString());

                if (result.has("exception")) {
                    throw new IOException(MessageFormat.format("Remote exception: {0}.", result.getString("exception")));
                } else {
                    return result;
                }
            } else {
                throw new IOException(MessageFormat.format("HTTP response error: {0}. {1}", Integer.toString(responsecode), connection.getResponseMessage()));
            }
        } catch (JSONException ex) {
            throw new IOException(MessageFormat.format("Parse exception: {0}.", ex.getMessage()));
        } finally {
            if (writerout != null) {
                writerout.close();
                writerout = null;
            }
            if (readerin != null) {
                readerin.close();
                readerin = null;
            }
        }
    }
}
