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
}
