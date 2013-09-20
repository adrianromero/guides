package com.adrguides.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by adrian on 20/08/13.
 */
public class Place implements Parcelable {

    private String id; // nulllable
    private String title; // not null

    private List<Section> sections = new ArrayList<Section>();

    public Place() {
        id = null;
        title = "* * *";
    }

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

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
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
        parcel.writeTypedList(getSections());
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            Place place = new Place();
            place.setId(in.readString());
            place.setTitle(in.readString());
            place.setSections(in.createTypedArrayList(Section.CREATOR));
            return place;
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
