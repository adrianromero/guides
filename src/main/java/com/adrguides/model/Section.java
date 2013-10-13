package com.adrguides.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adrian on 4/09/13.
 */
public class Section implements Parcelable {

    private String text = ""; // not null
    private String read = ""; // Nullable. If null then the text to read is "text"
    private String imageURL = null; // Nullable

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getText());
        parcel.writeString(getRead());
        parcel.writeString(getImageURL());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Section> CREATOR = new Parcelable.Creator<Section>() {
        public Section createFromParcel(Parcel in) {
            Section section = new Section();
            section.setText(in.readString());
            section.setRead(in.readString());
            section.setImageURL(in.readString());
            return section;
        }

        public Section[] newArray(int size) {
            return new Section[size];
        }
    };
}
