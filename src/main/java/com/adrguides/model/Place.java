package com.adrguides.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adrian on 20/08/13.
 */
public class Place implements Parcelable {

    private String[] text;
    private String title; // not null
    private String id; // nulllable

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVisibleLabel() {
        return (id == null ? "" : id + " - ") + title;
    }

    public String[] getText() {
        return text;
    }

    public void setText(String[] text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getId());
        parcel.writeString(getTitle());
        parcel.writeStringArray(getText());
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            Place place = new Place();
            place.setId(in.readString());
            place.setTitle(in.readString());
            place.setText(in.createStringArray());
            return place;
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
