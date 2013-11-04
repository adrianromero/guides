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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by adrian on 4/11/13.
 */
public class AlertFragment extends DialogFragment {

    public final static String TITLE = "ARG_TITLE";
    public final static String MESSAGE = "ARG_MESSAGE";

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(AlertFragment dialog);
        public void onDialogNegativeClick(AlertFragment dialog);
    }

    NoticeDialogListener listener = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()  + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(this.getArguments().getString(AlertFragment.TITLE));
        builder.setMessage(this.getArguments().getString(AlertFragment.MESSAGE));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (listener != null) {
                    listener.onDialogPositiveClick(AlertFragment.this);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (listener != null) {
                    listener.onDialogNegativeClick(AlertFragment.this);
                }
            }
        });
        return builder.create();
    }
}