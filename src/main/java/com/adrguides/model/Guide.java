package com.adrguides.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by adrian on 20/08/13.
 */
public class Guide implements Parcelable {

    private String title;
    private String language;
    private String country;
    private String variant;

    private List<Place> places = new ArrayList<Place>();

    public Guide() {
        title = "* * *";
        language = Locale.getDefault().getLanguage();
        country = Locale.getDefault().getCountry();
        variant = Locale.getDefault().getVariant();
    }

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

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
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
        parcel.writeTypedList(getPlaces());
    }

    public static final Parcelable.Creator<Guide> CREATOR = new Parcelable.Creator<Guide>() {
        public Guide createFromParcel(Parcel in) {
            Guide guide = new Guide();
            guide.setTitle(in.readString());
            guide.setLanguage(in.readString());
            guide.setCountry(in.readString());
            guide.setVariant(in.readString());
            guide.setPlaces(in.createTypedArrayList(Place.CREATOR));
            return guide;
        }

        public Guide[] newArray(int size) {
            return new Guide[size];
        }
    };
}
