package com.adrguides.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by adrian on 20/08/13.
 */
public class Guide implements Parcelable {

    private String title = null;
    private String language = "en";
    private String country = "US";
    private String variant = "";

    private Place[] places = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public Locale getLocale() {
        return new Locale(language, country, variant);
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
        parcel.writeString(getLanguage());
        parcel.writeString(getCountry());
        parcel.writeString(getVariant());
        parcel.writeParcelableArray(getPlaces(), i);
    }

    public static final Parcelable.Creator<Guide> CREATOR = new Parcelable.Creator<Guide>() {
        public Guide createFromParcel(Parcel in) {
            Guide guide = new Guide();
            guide.setTitle(in.readString());
            guide.setLanguage(in.readString());
            guide.setCountry(in.readString());
            guide.setVariant(in.readString());
            Parcelable[] places = in.readParcelableArray(getClass().getClassLoader());
            guide.setPlaces(Arrays.copyOf(places, places.length, Place[].class));
            return guide;
        }

        public Guide[] newArray(int size) {
            return new Guide[size];
        }
    };
}
