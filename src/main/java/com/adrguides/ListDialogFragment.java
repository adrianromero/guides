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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;

import java.util.List;

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
        List<Place> places = guide.getPlaces();

        String[] labels = new String[places.size()];
        for (int i = 0; i < places.size(); i++) {
            labels[i] = places.get(i).getVisibleLabel();
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
