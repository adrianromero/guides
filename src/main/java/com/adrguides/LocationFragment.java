package com.adrguides;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.adrguides.model.Place;
import com.adrguides.tts.TextToSpeechSingleton;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by adrian on 19/08/13.
 */
public class LocationFragment extends Fragment implements TextToSpeechSingleton.PlayingListener {

    public final static String TAG = "LOCATION_FRAGMENT";

    private Place place;
    private int playing = -1;

    private View v;

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        place = getArguments().getParcelable(ReadGuideActivity.ARG_PLACE);

        v = inflater.inflate(R.layout.fragment_location, container, false);
        Button b;

        b = (Button) v.findViewById(R.id.btnPlay);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                doPlay(view);
            }
        });

        b = (Button) v.findViewById(R.id.btnStop);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                doStop(view);
            }
        });

        ((TextView) v.findViewById(R.id.textTitle)).setText(place.getTitle());

        return v;
    }

    public void onResume () {
        super.onResume();
        TextToSpeechSingleton.getInstance().setPlayingListener(this);
    }
    public void onPause () {
        super.onPause();
        TextToSpeechSingleton.getInstance().setPlayingListener(null);
    }

    public void onSaveInstanceState (Bundle outState) {
        outState.putInt("Playing", playing);
    }
    private void doPlay(View view) {

        TextToSpeechSingleton.getInstance().getTTS().setLanguage(new Locale("spa", "ESP"));

        for (int i = 0; i < place.getText().length; i++) {
            String paragraph = place.getText()[i];
            HashMap<String, String> ttsparams = new HashMap<String, String>();
            ttsparams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Integer.toString(i));
            TextToSpeechSingleton.getInstance().getTTS().speak(place.getText()[i], i == 0 ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, ttsparams);
        }
    }

    private void doStop(View view) {

        TextToSpeechSingleton.getInstance().getTTS().stop();
    }

    @Override
    public void playing(final String s) {
        v.post(new Runnable(){
            public void run() {
                Log.println(0, "com.adrguides.LocationFragment", "playing -> " + s);
                if (s == null) {
                    playing = -1;
                    ((TextView) v.findViewById(R.id.textContent)).setText(null);
                } else {
                    playing = Integer.parseInt(s);
                    ((TextView) v.findViewById(R.id.textContent)).setText(place.getText()[playing]);
                }
            }
        });

    }
}
