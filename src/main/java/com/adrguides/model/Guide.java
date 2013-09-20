//        Guidebook is an Android application that reads audioguides using Text-to-Speech services.
//        Copyright (C) 2013  Adrián Romero Corchado
//
//        This program is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        This program is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.adrguides.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
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
