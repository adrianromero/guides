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

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.tts.TextToSpeechSingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by adrian on 19/08/13.
 */
public class LocationFragment extends Fragment implements TextToSpeechSingleton.PlayingListener {

    public final static String TAG = "LOCATION_FRAGMENT";

    private View v;
    private SearchViewGuides searchview;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        v = inflater.inflate(R.layout.fragment_location, container, false);

        return v;
    }

    public void onResume () {
        super.onResume();

        TextToSpeechSingleton.getInstance().setPlayingListener(this);
        printStatus();
    }
    public void onPause () {
        super.onPause();

        TextToSpeechSingleton.getInstance().setPlayingListener(null);
    }
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.read_guide, menu);

        menu.findItem(R.id.action_playpause).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                TextToSpeechSingleton.getInstance().playstartpause();
                return true;
            }
        });

        menu.findItem(R.id.action_next).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                TextToSpeechSingleton.getInstance().gotoNext();
                return true;
            }
        });

        menu.findItem(R.id.action_first).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                TextToSpeechSingleton.getInstance().gotoFirst();
                return true;
            }
        });
        menu.findItem(R.id.action_previous).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                TextToSpeechSingleton.getInstance().gotoPrevious();
                return true;
            }
        });
        menu.findItem(R.id.action_list).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new ListDialogFragment().show(getFragmentManager(), ListDialogFragment.TAG);
                return true;
            }
        });

        searchview = new SearchViewGuides(this.getActivity(), menu.findItem(R.id.menu_search));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean playing = TextToSpeechSingleton.getInstance().isPlaying();

        MenuItem playpause = menu.findItem(R.id.action_playpause);
        playpause.setTitle(playing ? R.string.action_pause : R.string.action_play);
        playpause.setIcon(playing ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
    }

    private void printStatus() {

        Guide guide = TextToSpeechSingleton.getInstance().getGuide();
        int chapter = TextToSpeechSingleton.getInstance().getChapter();
        int paragraph = TextToSpeechSingleton.getInstance().getParagraph();
        // boolean playing = TextToSpeechSingleton.getInstance().isPlaying();

        TextView title = (TextView) v.findViewById(R.id.textTitle);
        TextView content = (TextView) v.findViewById(R.id.textContent);

        if (guide == null || chapter >= guide.getPlaces().length) {
            title.setVisibility(View.GONE);
            content.setVisibility(View.GONE);
            switchImage(null);
        } else {
            Place mychapter = guide.getPlaces()[chapter];
            title.setVisibility(View.VISIBLE);
            title.setText(mychapter.getVisibleLabel());
            if (paragraph >= 0) {
                content.setVisibility(View.VISIBLE);
                content.setText(mychapter.getSections()[paragraph].getText());
                Bitmap b = mychapter.getSections()[paragraph].getImage();
                if (b == null) {
                    b = mychapter.getImage();
                }
                switchImage(b);
            } else {
                content.setVisibility(View.GONE);
                Bitmap b = mychapter.getImage();
                if (b == null && mychapter.getSections().length > 0) {
                    b = mychapter.getSections()[0].getImage();
                }
                switchImage(b);
            }
        }

        getActivity().invalidateOptionsMenu();
    }

    private Bitmap currentImage = null;
    private void switchImage(Bitmap image) {

        if (image == currentImage) {
            return;
        }

        currentImage = image;

        ViewSwitcher switcher = (ViewSwitcher) v.findViewById(R.id.switcherBackgroundGuide);

        ImageView iv0 = (ImageView) v.findViewById(R.id.imageBackgroundGuide_0);
        ImageView iv1 = (ImageView) v.findViewById(R.id.imageBackgroundGuide_1);

        if (switcher.getDisplayedChild() == 0) {
            if (image == null) {
                iv1.setImageResource(R.drawable.place_default);
            } else {
                iv1.setImageBitmap(currentImage);
            }
            switcher.showNext();
        } else {
            if (image == null) {
                iv0.setImageResource(R.drawable.place_default);
            } else {
                iv0.setImageBitmap(currentImage);
            }
            switcher.showPrevious();
        }
    }

    @Override
    public void update() {
        v.post(new Runnable(){
            public void run() {
                printStatus();
            }
        });
    }
}
