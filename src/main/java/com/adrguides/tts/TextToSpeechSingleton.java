package com.adrguides.tts;

/**
 * Created by adrian on 22/08/13.
 */
public class TextToSpeechSingleton {
    private static TextToSpeechSingleton ourInstance = new TextToSpeechSingleton();

    public static TextToSpeechSingleton getInstance() {
        return ourInstance;
    }

    private TextToSpeechSingleton() {
    }
}
