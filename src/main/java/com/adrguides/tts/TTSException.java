package com.adrguides.tts;

import android.content.Context;

/**
 * Created by adrian on 7/09/13.
 */
public class TTSException extends Exception {

    private Context context;
    private int resource;

    public TTSException(String msg) {
        super(msg);
    }
}
