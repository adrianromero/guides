package com.adrguides.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adrian on 20/08/13.
 */
public class Place implements Parcelable {

    private Section[] sections;
    private String title; // not null
    private String id; // nulllable
    private Bitmap image; // nullable

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

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
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
        parcel.writeParcelable(getImage(), i);
        parcel.writeParcelableArray(getSections(), i);
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            Place place = new Place();
            place.setId(in.readString());
            place.setTitle(in.readString());
            place.setImage((Bitmap) in.readParcelable(null));
            place.setSections((Section[]) in.readParcelableArray(null));
            return place;
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
