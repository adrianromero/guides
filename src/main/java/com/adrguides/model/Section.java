package com.adrguides.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adrian on 4/09/13.
 */
public class Section implements Parcelable {

    private String text = null; // not null
    private String image = null; // Nullable

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getText());
        parcel.writeString(getImage());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Section> CREATOR = new Parcelable.Creator<Section>() {
        public Section createFromParcel(Parcel in) {
            Section section = new Section();
            section.setText(in.readString());
            section.setImage(in.readString());
            return section;
        }

        public Section[] newArray(int size) {
            return new Section[size];
        }
    };
}
