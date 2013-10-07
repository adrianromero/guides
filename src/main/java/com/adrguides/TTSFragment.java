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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;

import java.util.HashMap;

/**
 * Created by adrian on 7/09/13.
 */
public class TTSFragment extends Fragment implements TextToSpeech.OnInitListener {

    public static final String TAG = "TTSFragment-Tag";

    // On create members
    private SharedPreferences sharedPref;
    private TextToSpeech tts = null;
    private boolean initialized = false;

    // Media
    private Guide guide = null;
    private int chapter = 0;
    private int paragraph = 0;
    private boolean playing = false;
    private String playing_last = null;

    private boolean stopping = false;

    private PlayingListener playinglistener = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        // Loading TTS engine
        tts = new TextToSpeech(getActivity().getApplicationContext(), this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onInit(int i) {

        initialized = true;
        if (TextToSpeech.SUCCESS != i) {
            tts = null;
        } else {
            tts.setOnUtteranceProgressListener(new UtteranceListener());
        }
        fireUpdate();
    }

    public Guide getGuide() {
        return guide;
    }

    public int getChapter() {
        return chapter;
    }

    public int getParagraph() {
        return paragraph;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isTTSReady() {
        return (initialized && tts != null);
    }
    public boolean isGuideAvailable() {
        return isTTSReady() && guide != null;
    }
    public boolean isGuideLanguageAvailable() {
        return isTTSReady() && isGuideAvailable() && tts.isLanguageAvailable(guide.getLocale()) >= 0;
    }

    public boolean isTTSError() {
        return initialized && tts == null;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void playGuide(Guide guide) {
        playGuide(guide, 0, -1);
    }

    private void playGuide(Guide guide, int chapter) {
        playGuide(guide, chapter, -1);
    }


    private void playGuide(Guide guide, int chapter, int paragraph) {

        if (isTTSReady()) {
            tts.stop();
            playing = false;
        }

        this.guide = guide;
        this.chapter = chapter;
        this.paragraph = paragraph;

        fireUpdate();
    }

    public void playstartpause() {

        if (playing) {
            stopping = true;
            tts.stop();
            playing = false;
        } else {
            playresume();
        }
    }

    public void gotoChapter(int i) {

        tts.stop();
        playing = false;

        if (i >= 0 && i < guide.getPlaces().size()) {
            chapter = i;
            paragraph = -1;
        }

        fireUpdate();

        //
        if (sharedPref.getBoolean("pref_gdi_autoplay", false)) {
            playresume();
        }
    }

    public boolean isEnabledPrevious() {
        return paragraph >= 0 || chapter > 0;
    }

    public void gotoPrevious() {

        if (paragraph == -1) {
            // if we are in the begining of a chapter go to previous chapter
            gotoChapter(chapter -1);
        } else {
            // if we are not in the begining of a chapter go to the begining of the chapter
            gotoChapter(chapter);
        }
    }

    public boolean isEnabledNext() {
        return chapter < guide.getPlaces().size() - 1;
    }

    public void gotoNext() {

        gotoChapter(chapter + 1);
    }

    public void gotoFirst() {
        gotoChapter(0);
    }

    private float calculateLogValue(int value, double exponent, double factor) {
        return (float) Math.pow(exponent, value * factor);
    }

    private void playresume() {

        tts.stop();
        playing = false;

        if (chapter < guide.getPlaces().size()) {

            tts.setLanguage(guide.getLocale());
            tts.setPitch(calculateLogValue(sharedPref.getInt("pref_gdi_pitch", 0), 5.0, 0.1));
            tts.setSpeechRate(calculateLogValue(sharedPref.getInt("pref_gdi_speechrate", 0), 3.0, 0.1));


            Place place = guide.getPlaces().get(chapter);

            if (paragraph < 0) {
                paragraph = 0;
            }
            for (int i = paragraph; i < place.getSections().size(); i++) {

                Log.d("com.adrguides.TTS", "playing --> " + Integer.toString(i) + " = " + place.getSections().get(i).getText());
                String textparagraph = place.getSections().get(i).getText();
                HashMap<String, String> ttsparams = new HashMap<String, String>();
                ttsparams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Integer.toString(i));
                tts.speak(textparagraph, i == 0 ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, ttsparams);
                playing_last = Integer.toString(i);
            }
        }
    }

    public void setPlayingListener(PlayingListener playinglistener) {
        this.playinglistener = playinglistener;
    }

    private class UtteranceListener extends UtteranceProgressListener {
        @Override
        public void onStart(String s) {
            playing = true;
            paragraph = Integer.parseInt(s);
            fireUpdate();
        }
        @Override
        public void onDone(String s) {
            playing = false;
            if (stopping) {
                stopping = false;
                fireUpdate();
            } else if (s.equals(playing_last)) {
                playing_last = null;
                paragraph = -1;
                fireUpdate();
            } // else do nothing because is not the last and then it will start a new one
        }
        @Override
        public void onError(String s) {
            playing = false;
            if (stopping) {
                stopping = false;
                fireUpdate();
            } else if (s.equals(playing_last)) {
                playing_last = null;
                paragraph = -1;
                fireUpdate();
            } // else do nothing because is not the last and then it will start a new one
        }
    }

    private void fireUpdate() {
        if (playinglistener != null) {
            playinglistener.update();
        }
    }

    public static interface PlayingListener {
        public void update(); // should be called only if change playlist
    }
}
