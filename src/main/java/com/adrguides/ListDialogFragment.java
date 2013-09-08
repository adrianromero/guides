package com.adrguides;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.tts.TextToSpeechSingleton;

/**
 * Created by adrian on 31/08/13.
 */
public class ListDialogFragment extends DialogFragment {

    public static final String TAG = "LIST_DIALOG_FRAGMENT";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        final TTSFragment ttsfragment = (TTSFragment) getFragmentManager().findFragmentByTag(TTSFragment.TAG);

        Guide guide = ttsfragment.getGuide();
        Place[] places = guide.getPlaces();

        String[] labels = new String[places.length];
        for (int i = 0; i < places.length; i++) {
            labels[i] = places[i].getVisibleLabel();
        }

        builder.setTitle(guide.getTitle());
        builder.setItems(labels, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ttsfragment.gotoChapter(which);
            }
        });
        return builder.create();
    }

}
