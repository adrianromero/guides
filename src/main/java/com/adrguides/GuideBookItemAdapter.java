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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by adrian on 21/10/13.
 */
public class GuideBookItemAdapter extends ArrayAdapter<GuideBookItem> {

    private LayoutInflater myinflater;

    public GuideBookItemAdapter(Context context) {
        super(context, R.layout.item_guide);
        myinflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        return createViewFromResource(position, convertView, parent, R.layout.item_guide);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent,
                                        int resource) {
        View view;

        if (convertView == null) {
            view = myinflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        GuideBookItem item = getItem(position);

        TextView textItemTitle = (TextView) view.findViewById(R.id.textItemTitle);
        textItemTitle.setText(item.getTitle());

        TextView textDescription = (TextView) view.findViewById(R.id.textDescription);
        textDescription.setText(item.getDescription());
        TextView textAuthor = (TextView) view.findViewById(R.id.textAuthor);
        textAuthor.setText(item.getAuthor());
        TextView textLanguage = (TextView) view.findViewById(R.id.textLanguage);
        textLanguage.setText(item.getLocaleName());

        ImageView imageItem = (ImageView) view.findViewById(R.id.imageItem);

        Bitmap bmp;
        if (item.getImageFile() == null || item.getImageFile().equals("")) {
            bmp = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.ic_launcher);
        } else {
            bmp = BitmapFactory.decodeFile(item.getImageFile());
        }

        imageItem.setImageBitmap(bmp);

        return view;
    }
}
