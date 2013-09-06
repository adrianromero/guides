package com.adrguides;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentBreadCrumbs;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.tts.TextToSpeechSingleton;


public class ReadGuideActivity extends Activity implements FragmentManager.OnBackStackChangedListener {

    private static final int TTS_REQUEST_CODE = 332342;

    public static final String ARG_GUIDE = "ARG_GUIDE";
    public static final String ARG_PLACE = "ARG_PLACE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);


        TextToSpeechSingleton.getInstance().init(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this));

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        return sharedPref.getString("pref_rpi_url", "");


        FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener(this);

        Fragment locationfragment = fm.findFragmentByTag(LocationFragment.TAG);
        if (locationfragment == null) {
            locationfragment = new LocationFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.guiderootview, locationfragment, LocationFragment.TAG);
            ft.commit();
        }

        updateBreadCrumbs();

//        Intent checkIntent = new Intent();
//        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkIntent, TTS_REQUEST_CODE);
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

    private void updateBreadCrumbs() {

        Guide guide = TextToSpeechSingleton.getInstance().getGuide();
        int chapter = TextToSpeechSingleton.getInstance().getChapter();
        if (guide == null) {
            getActionBar().setTitle(getResources().getText(R.string.title_activity_read_guide));
        } else {
            getActionBar().setTitle(guide.getTitle());
        }
    }

    @Override
    public void onBackStackChanged() {
        updateBreadCrumbs();
    }
}
