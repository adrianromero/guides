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

    private static final int TTS_REQUEST_CODE = 332342;

    public static final String ARG_GUIDE = "ARG_GUIDE";
    // public static final String ARG_PLACE = "ARG_PLACE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        FragmentManager fm = getFragmentManager();

        LoadGuideFragment loadguide = (LoadGuideFragment) fm.findFragmentByTag(LoadGuideFragment.TAG);
        if (loadguide == null) {
            // Calculate rezize dimensions
            Point size = new Point();
            this.getWindowManager().getDefaultDisplay().getSize(size);
            int imagesize = Math.max(size.x, size.y);
            // loading guide
            loadguide = new LoadGuideFragment();
            fm.beginTransaction().add(loadguide, LoadGuideFragment.TAG).commit();

            Log.d("com.adrguides.LoadActivity", "Loading Data --> " + getIntent().getDataString());
            loadguide.loadGuide(getApplicationContext(), getIntent().getDataString(), imagesize);

            Fragment loadfragment = new LoadFragment();
            fm.beginTransaction()
                    .add(android.R.id.content, loadfragment, LoadFragment.TAG)
                    .commit();
        }



//        TTSFragment ttsfragment = (TTSFragment) fm.findFragmentByTag(TTSFragment.TAG);
//        if (ttsfragment == null) {
//            ttsfragment = new TTSFragment();
//            fm.beginTransaction()
//                    .add(ttsfragment, TTSFragment.TAG)
//                    .commit();
//        }
//
//        Fragment locationfragment = fm.findFragmentByTag(ReadGuideFragment.TAG);
//        if (locationfragment == null) {
//            locationfragment = new ReadGuideFragment();
//            fm.beginTransaction()
//                    .add(android.R.id.content, locationfragment, ReadGuideFragment.TAG)
//                    .commit();
//        }

//        Intent checkIntent = new Intent();
//        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkIntent, TTS_REQUEST_CODE);
    }

    public void loadGuide(Guide guide) {

        Log.d("com.adrguides.ReadGuideActivity", "loadGuide");
        FragmentManager fm = getFragmentManager();

        TTSFragment ttsfragment = (TTSFragment) fm.findFragmentByTag(TTSFragment.TAG);
        if (ttsfragment == null) {
            ttsfragment = new TTSFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("GUIDE", guide);
            ttsfragment.setArguments(bundle);
            fm.beginTransaction()
                    .add(ttsfragment, TTSFragment.TAG)
                    .commit();
        }

        openReadGuideFragment();
    }

    public void openReadGuideFragment() {

        Log.d("com.adrguides.ReadGuideActivity", "openReadGuideFragment");
        FragmentManager fm = getFragmentManager();
        Fragment locationfragment = fm.findFragmentByTag(ReadGuideFragment.TAG);
        if (locationfragment == null) {
            locationfragment = new ReadGuideFragment();
            fm.beginTransaction()
                    .replace(android.R.id.content, locationfragment, ReadGuideFragment.TAG)
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == TTS_REQUEST_CODE) {
//            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
//                // success, create the TTS instance
//                tts = new TextToSpeech(this, this);
//            } else {
//
////                // missing data, install it
////                Intent installIntent = new Intent();
////                installIntent.setAction(
////                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
////                startActivity(installIntent);
//                tts = null;
//            }
//        }
//    }

}
