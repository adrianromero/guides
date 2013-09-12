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

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final int TTS_REQUEST_CODE = 332342;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);



        SpinnerAdapter mSpinnerAdapter = new ArrayAdapter(getActionBar().getThemedContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[] {"one","two","three"});

        ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {

                return true;
            }
        };

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
    }
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.shutdown();
        }
        super.onDestroy();
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


    //// Test code









    public void onReadActivityClicked(View view) {
        Intent intent = new Intent(this, ReadGuideActivity.class);
        startActivity(intent);
    }

    public void onLoadActivityClicked(View view) {
        Intent intent = new Intent(this, LoadActivity.class);
        intent.putExtra(LoadActivity.GUIDE_URL, "mockguide");
        intent.putExtra(LoadActivity.GUIDE_NAME, "La guía de los mocos");
        startActivity(intent);
    }

    public void onCheckTTSClicked(View view) {

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_REQUEST_CODE);

        TextView msg = (TextView) this.findViewById(R.id.messageText);
        msg.setText("checking");
    }

    public void onTalkClicked(View view) {
        String myText1 = "¿Has dormido bién?";
        String myText2 = "Eso espero, porque es hora de levantarse.";

        HashMap<String, String> ttsparams = new HashMap<String, String>();
        ttsparams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "end of wakeup message ID");
        tts.speak(myText1, TextToSpeech.QUEUE_FLUSH, ttsparams);
        tts.speak(myText2, TextToSpeech.QUEUE_ADD, ttsparams);
    }

    public void onInstallTTSClicked(View view) {
        Intent installIntent = new Intent();
        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        startActivity(installIntent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_REQUEST_CODE) {
            TextView msg = (TextView) this.findViewById(R.id.messageText);
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = new TextToSpeech(this, this);


                msg.setText("loading");
            } else {

//                // missing data, install it
//                Intent installIntent = new Intent();
//                installIntent.setAction(
//                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                startActivity(installIntent);
                msg.setText("error");
            }
        }
    }

    @Override
    public void onInit(int i) {
        TextView msg = (TextView) this.findViewById(R.id.messageText);
        msg.setText("success");

//        tts.setLanguage(Locale.US);
//        tts.isLanguageAvailable(new Locale("spa", "ESP"));

//        tts.setOnUtteranceProgressListener(new UtteranceListener());

        List<TextToSpeech.EngineInfo> engines = tts.getEngines();
        Log.d("com.adrguides.MainActivity", "TTS Engines >>");
        for (TextToSpeech.EngineInfo e : engines) {
            Log.d("com.adrguides.MainActivity", e.toString());
        }
        Log.d("com.adrguides.MainActivity", "<< TTS Engines");

        tts.setLanguage(new Locale("spa", "ESP"));
        if (tts.isSpeaking()) {
            msg.setText("speaking");
        }
    }

//    private class UtteranceListener extends UtteranceProgressListener {
//        @Override
//        public void onStart(String s) {
//
//        }
//        @Override
//        public void onDone(String s) {
//
//        }
//        @Override
//        public void onError(String s) {
//
//        }
//    }
}
