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

package com.adrguides;

/**
 * Created by adrian on 21/10/13.
 */
public class GuideBookItem implements Comparable<GuideBookItem> {

    private String folder;
    private String uri;
    private String title;
    private String localename;
    private String imagefile;

    public GuideBookItem(String folder, String uri, String title, String localename, String imagefile) {
        this.folder = folder;
        this.uri = uri;
        this.title = title;
        this.localename = localename;
        this.imagefile = imagefile;
    }

    public String getFolder() {
        return folder;
    }

    public String getURI() {
        return uri;
    }

    public String getTitle() {
        return title;
    }

    public String getLocaleName() {
        return localename;
    }

    public String getImageFile() {
        return imagefile;
    }

    @Override
    public String toString() {
        return title;
    }

    private String comparer() {
        return title + "\n\n\n\n" + localename;
    }

    @Override
    public int compareTo(GuideBookItem guidebookitem) {
        return comparer().compareTo(guidebookitem.comparer());
    }
}
