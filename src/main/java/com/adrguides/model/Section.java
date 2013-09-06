package com.adrguides.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adrian on 4/09/13.
 */
public class Section implements Parcelable {

    private String text = null; // not null
    private Bitmap image = null; // Nullable

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getText());
        parcel.writeParcelable(getImage(), i);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Section> CREATOR = new Parcelable.Creator<Section>() {
        public Section createFromParcel(Parcel in) {
            Section section = new Section();
            section.setText(in.readString());
            section.setImage((Bitmap) in.readParcelable(null));
            return section;
        }

        public Section[] newArray(int size) {
            return new Section[size];
        }
    };
}
