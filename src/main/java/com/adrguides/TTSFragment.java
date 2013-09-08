package com.adrguides;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;

import java.util.HashMap;
import java.util.Locale;

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

        tts = new TextToSpeech(getActivity().getApplicationContext(), this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onInit(int i) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

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

    public void setGuide(Guide guide) {
        setGuide(guide, 0, -1);
    }

    public void setGuide(Guide guide, int chapter) {
        setGuide(guide, chapter, -1);
    }


    public void setGuide(Guide guide, int chapter, int paragraph) {

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

        if (i >= 0 && i < guide.getPlaces().length) {
            chapter = i;
            paragraph = -1;
        }

        fireUpdate();

        //
        if (sharedPref.getBoolean("pref_gdi_autoplay", false)) {
            playresume();
        }
    }

    public void gotoPrevious() {

        gotoChapter(chapter - 1);
    }

    public void gotoNext() {

        gotoChapter(chapter + 1);
    }

    public void gotoFirst() {
        gotoChapter(0);
    }

    public void restartChapter() {
        gotoChapter(chapter);
    }

    private float calculateLogValue(int value, double exponent, double factor) {
        return (float) Math.pow(exponent, value * factor);
    }

    private void playresume() {

        tts.stop();
        playing = false;

        if (chapter < guide.getPlaces().length) {

            tts.setLanguage(guide.getLocale());
            tts.setPitch(calculateLogValue(sharedPref.getInt("pref_gdi_pitch", 0), 5.0, 0.1));
            tts.setSpeechRate(calculateLogValue(sharedPref.getInt("pref_gdi_speechrate", 0), 3.0, 0.1));


            Place place = guide.getPlaces()[chapter];

            if (paragraph < 0) {
                paragraph = 0;
            }
            for (int i = paragraph; i < place.getSections().length; i++) {

                Log.d("com.adrguides.TTS", "playing --> " + Integer.toString(i) + " = " + place.getSections()[i].getText());
                String textparagraph = place.getSections()[i].getText();
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
