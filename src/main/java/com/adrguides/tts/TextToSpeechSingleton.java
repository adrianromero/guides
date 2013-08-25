package com.adrguides.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

/**
 * Created by adrian on 22/08/13.
 */
public class TextToSpeechSingleton implements TextToSpeech.OnInitListener {
    private static TextToSpeechSingleton ourInstance = new TextToSpeechSingleton();


    private TextToSpeech tts = null;
    private String playing = null;
    private boolean initialized = false;

    private PlayingListener playinglistener = null;

    public static TextToSpeechSingleton getInstance() {
        return ourInstance;
    }

    private TextToSpeechSingleton() {
    }

    public void init(Context context) {

        if (!initialized && tts == null) {
            tts = new TextToSpeech(context, this);
        }
    }

    public TextToSpeech getTTS() {
        return tts;
    }

    public String getPlaying() {
        return playing;
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
            playing = s;
            if (playinglistener != null) {
                playinglistener.playing(playing);
            }
        }
        @Override
        public void onDone(String s) {
            playing = null;
            if (playinglistener != null) {
                playinglistener.playing(playing);
            }
        }
        @Override
        public void onError(String s) {
            playing = null;
            if (playinglistener != null) {
                playinglistener.playing(playing);
            }
        }
    }

    public static interface PlayingListener {
        public void playing(String s);
    }
}
