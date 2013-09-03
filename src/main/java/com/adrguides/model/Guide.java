package com.adrguides.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by adrian on 20/08/13.
 */
public class Guide implements Parcelable {

    private String title = null;
    private Place[] places = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Place[] getPlaces() {
        return places;
    }

    public void setPlaces(Place[] places) {
        this.places = places;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getTitle());
        parcel.writeParcelableArray(getPlaces(), i);
    }

    public static final Parcelable.Creator<Guide> CREATOR = new Parcelable.Creator<Guide>() {
        public Guide createFromParcel(Parcel in) {
            Guide guide = new Guide();
            guide.setTitle(in.readString());
            guide.setPlaces((Place[]) in.readParcelableArray(null));
            return guide;
        }

        public Guide[] newArray(int size) {
            return new Guide[size];
        }
    };


    public static final Guide createFromJSON(JSONObject json) throws JSONException {

        Guide guide = new Guide();
        guide.setTitle(json.getString("title"));

        JSONArray chapters = json.getJSONArray("chapters");
        Place[] places = new Place[chapters.length()];
        guide.setPlaces(places);
        for (int i = 0; i < chapters.length(); i++) {
            JSONObject chapter = chapters.getJSONObject(i);
            Place p = new Place();
            p.setId(chapter.has("id") ? chapter.getString("id") : null);
            p.setTitle(chapter.getString("title"));

            JSONArray paragraphs = chapter.getJSONArray("paragraphs");
            String[] strs = new String[paragraphs.length()];
            p.setText(strs);
            for (int j = 0; j < paragraphs.length(); j++) {
                strs[j] = paragraphs.getString(j);
            }
            places[i] = p;
        }
        return guide;
    }
}
