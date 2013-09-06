package com.adrguides.tts;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by adrian on 22/08/13.
 */
public class TextToSpeechSingleton implements TextToSpeech.OnInitListener {

    private static TextToSpeechSingleton ourInstance = new TextToSpeechSingleton();

    public static TextToSpeechSingleton getInstance() {
        return ourInstance;
    }

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

    private TextToSpeechSingleton() {
    }

    public void init(Context context, SharedPreferences sharedPref) {

        // SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.sharedPref = sharedPref;

        if (!initialized && tts == null) {
            tts = new TextToSpeech(context, this);
        }
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

    public void setGuide(Guide guide) {
        setGuide(guide, 0, -1);
    }
    public void setGuide(Guide guide, int chapter) {
        setGuide(guide, chapter, -1);
    }
    public void setGuide(Guide guide, int chapter, int paragraph) {

        tts.stop();
        playing = false;

        this.guide = guide;
        this.chapter = chapter;
        this.paragraph = paragraph;
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

    private void playresume() /* throws TTSException */ {

        tts.stop();
        playing = false;

        if (chapter < guide.getPlaces().length) {

            tts.setLanguage(new Locale("es", "ES"));

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

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onInit(int i) {

        initialized = true;
        if (TextToSpeech.SUCCESS != i) {
            tts = null;
            return;
        }
        tts.setOnUtteranceProgressListener(new UtteranceListener());
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
        Log.d("com.adrguides.TTS", "updating -> " + this.getChapter() + " / " + this.getParagraph());
        if (playinglistener != null) {
            playinglistener.update();
        }
    }

    public static interface PlayingListener {
        public void update(); // should be called only if change playlist
    }
}
