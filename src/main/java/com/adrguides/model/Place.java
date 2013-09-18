package com.adrguides.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by adrian on 20/08/13.
 */
public class Place implements Parcelable {

    private Section[] sections;
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

    public Section[] getSections() {
        return sections;
    }

    public void setSections(Section[] sections) {
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
        parcel.writeParcelableArray(getSections(), i);
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            Place place = new Place();
            place.setId(in.readString());
            place.setTitle(in.readString());
            Parcelable[] sections = in.readParcelableArray(getClass().getClassLoader());
            place.setSections(Arrays.copyOf(sections, sections.length, Section[].class));
            return place;
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
