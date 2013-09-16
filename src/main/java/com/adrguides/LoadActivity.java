//        Guides is an Android application that reads audioguides using Text-to-Speech services.
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
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.adrguides.model.Guide;
import com.adrguides.tts.TextToSpeechSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;

/**
 * Created by adrian on 2/09/13.
 */
public class LoadActivity extends Activity implements LoadGuideFragment.LoadGuideCallbacks {

    public static final String GUIDE_URL = "GUIDE_URL";
    public static final String GUIDE_NAME = "GUIDE_NAME";

    private LoadGuideFragment loadguide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        if (savedInstanceState != null) {
            ((TextView)findViewById(R.id.textException)).setText(savedInstanceState.getCharSequence("_textException"));
            if (savedInstanceState.getInt("_displayedChild") == 1) {
                ((ViewSwitcher) findViewById(R.id.mySwitcher)).showNext();
            }
        }

        FragmentManager fm = getFragmentManager();
        loadguide = (LoadGuideFragment) fm.findFragmentByTag(LoadGuideFragment.TAG);
        if (loadguide == null) {
            // Calculate rezize dimensions
            Point size = new Point();
            this.getWindowManager().getDefaultDisplay().getSize(size);
            int imagesize = Math.max(size.x, size.y);
            // loading guide
            loadguide = new LoadGuideFragment();
            fm.beginTransaction().add(loadguide, LoadGuideFragment.TAG).commit();
            loadguide.loadGuide(getApplicationContext(), getIntent().getStringExtra(GUIDE_URL), imagesize);
        }

        ((TextView) findViewById(R.id.textGuideName)).setText(getResources().getString(R.string.msg_loading, getIntent().getStringExtra(GUIDE_NAME)));

    }

    @Override
    public void onStart() {
        super.onStart();
        loadguide.startListening();
    }
    @Override
    public void onStop() {
        super.onStop();
        loadguide.stopListening();
    }

    @Override
    public void  onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putCharSequence("_textException", ((TextView)findViewById(R.id.textException)).getText());
        savedInstanceState.putInt("_displayedChild", ((ViewSwitcher) findViewById(R.id.mySwitcher)).getDisplayedChild());
    }

    @Override
    public void onFinishLoad(LoadedGuide result) {

        if (result.getStatus() == 0) {
            TextToSpeechSingleton.getInstance().setGuide(result.getGuide());

            Intent intent = new Intent(this, ReadGuideActivity.class);
            startActivity(intent);
        } else {
            ((TextView)findViewById(R.id.textException)).setText(result.getException());
            ViewSwitcher sw = (ViewSwitcher) findViewById(R.id.mySwitcher);
            sw.showNext();
        }
    }
}
