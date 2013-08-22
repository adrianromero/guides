package com.adrguides;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.adrguides.model.Place;

import java.util.HashMap;

/**
 * Created by adrian on 19/08/13.
 */
public class LocationFragment extends Fragment {

    public final static String TAG = "LOCATION_FRAGMENT";

    private LocationHandler lhandler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            lhandler = (LocationHandler) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LocationHandler");
        }
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final Place place = getArguments().getParcelable(ReadGuideActivity.ARG_PLACE);

        View v = inflater.inflate(R.layout.fragment_location, container, false);
        Button b = (Button) v.findViewById(R.id.buttonlocation);
        b.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                HashMap<String, String> ttsparams = new HashMap<String, String>();
                ttsparams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "end of wakeup message ID");
                lhandler.getTTS().speak(place.getText()[0], TextToSpeech.QUEUE_FLUSH, ttsparams);
                // lhandler.getTTS().speak(myText2, TextToSpeech.QUEUE_ADD, ttsparams);

            }
        });

        ((TextView) v.findViewById(R.id.textTitle)).setText(place.getTitle());
        ((TextView) v.findViewById(R.id.textContent)).setText(place.getText()[0]);

        b.setText(place.getTitle());

        return v;
    }
}
