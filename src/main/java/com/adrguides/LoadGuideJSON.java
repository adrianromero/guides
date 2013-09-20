//        Guides is an Android application that reads audioguides using Text-to-Speech services.
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

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.model.Section;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * Created by adrian on 18/09/13.
 */
public class LoadGuideJSON extends LoadGuide {

    public LoadGuideJSON(Context context, URL baseurl, int imagesize) {
        super(context, baseurl, imagesize);
    }

    @Override
    protected Guide load_imp(String text) throws Exception {

        JSONObject data = new JSONObject(text);

        Guide guide = new Guide();
        guide.setTitle(data.getString("title"));
        guide.setLanguage(data.optString("language", "en"));
        guide.setCountry(data.optString("country", "US"));
        guide.setVariant(data.optString("variant", ""));

        JSONArray chapters = data.getJSONArray("chapters");
        for (int i = 0; i < chapters.length(); i++) {
            final JSONObject chapter = chapters.getJSONObject(i);
            final Place p = new Place();
            p.setId(chapter.has("id") ? chapter.getString("id") : null);
            p.setTitle(chapter.getString("title"));

            JSONArray paragraphs = chapter.getJSONArray("paragraphs");
            for (int j = 0; j < paragraphs.length(); j++) {
                final Section section = new Section();
                final JSONObject s = paragraphs.optJSONObject(j);
                if (s == null) {
                    section.setText(paragraphs.getString(j));
                } else {
                    section.setText(s.getString("text"));
                    section.setImage(loadImage(s.optString("image")));
                }
                p.getSections().add(section);
            }
            guide.getPlaces().add(p);
        }
        return guide;
    }
}
