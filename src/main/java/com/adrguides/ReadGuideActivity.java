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

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.adrguides.model.Guide;


public class ReadGuideActivity extends Activity {

    // private static final int TTS_REQUEST_CODE = 332342;

    public static final String ARG_GUIDE_TITLE = "ARG_GUIDE_TITLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        LoadGuideFragment loadguide = (LoadGuideFragment) getFragmentManager().findFragmentByTag(LoadGuideFragment.TAG);
        if (loadguide == null) {
            // It is the first time?
            loadGuide(getIntent().getDataString());
        }

//        Intent checkIntent = new Intent();
//        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkIntent, TTS_REQUEST_CODE);
    }

    public void loadGuide(String url) {

        FragmentManager fm = getFragmentManager();

        LoadGuideFragment loadguide = (LoadGuideFragment) fm.findFragmentByTag(LoadGuideFragment.TAG);
        if (loadguide == null) {
            loadguide = new LoadGuideFragment();
            fm.beginTransaction().add(loadguide, LoadGuideFragment.TAG).commit();
        }

        // Load the guide...
        Log.d("com.adrguides.ReadGuideActivity", "Loading Data --> " + url);
        // Calculate rezize dimensions
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int imagesize = Math.max(size.x, size.y);


        loadguide.loadGuide(getApplicationContext(), url, imagesize);

        // show Load Fragment
        Log.d("com.adrguides.ReadGuideActivity", "showReadGuideFragment");
        Fragment loadfragment = fm.findFragmentByTag(LoadFragment.TAG);
        if (loadfragment == null) {
            loadfragment = new LoadFragment();
            fm.beginTransaction()
                    .add(android.R.id.content, loadfragment, LoadFragment.TAG)
                    .commit();
        }
    }

    public void playGuide(Guide guide) {

        Log.d("com.adrguides.ReadGuideActivity", "loadGuide");
        FragmentManager fm = getFragmentManager();

        TTSFragment ttsfragment = (TTSFragment) fm.findFragmentByTag(TTSFragment.TAG);
        if (ttsfragment == null) {
            ttsfragment = new TTSFragment();
            fm.beginTransaction()
                    .add(ttsfragment, TTSFragment.TAG)
                    .commit();
        }

        ttsfragment.playGuide(guide);

        // Show Read Guide Fragment
        Log.d("com.adrguides.ReadGuideActivity", "showReadGuideFragment");
        Fragment readguidefragment = fm.findFragmentByTag(ReadGuideFragment.TAG);
        if (readguidefragment == null) {
            readguidefragment = new ReadGuideFragment();
            fm.beginTransaction()
                    .replace(android.R.id.content, readguidefragment, ReadGuideFragment.TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onSettingsClicked(MenuItem item) {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onAboutClicked(MenuItem item) {

        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
