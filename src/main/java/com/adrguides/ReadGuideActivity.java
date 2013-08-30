package com.adrguides;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentBreadCrumbs;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.tts.TextToSpeechSingleton;


public class ReadGuideActivity extends Activity implements FragmentManager.OnBackStackChangedListener {

    private static final int TTS_REQUEST_CODE = 332342;

    public static final String ARG_GUIDE = "ARG_GUIDE";
    public static final String ARG_PLACE = "ARG_PLACE";

    private static Guide getGuide() {

        Place p1 = new Place();
        p1.setId("001");
        p1.setTitle("Cuadro primero");
        p1.setText(new String[] {
                "Este es el cuadro primero. \nNo es el mejor de todos pero al menos va el primero y eso se nota. Y eso es algo que no todo el mundo sabe apreciar.",
                "Muy bonito.",
                "Y me gusta mucho"
        });

        Place p2 = new Place();
        p2.setId("002");
        p2.setTitle("Cuadro segundo");
        p2.setText(new String[] {
                "Este es el cuadro segundo.",
                "Muy abstracto.",
                "Y me gusta menos que el primero y más que el segundo."
        });


        Place p3 = new Place();
        p3.setId("003");
        p3.setTitle("Cuadro último");
        p3.setText(new String[] {
                "Este es el último.",
                "El mejor",
                "Y el que más me gusta."
        });
        Guide guide = new Guide();
        guide.setTitle("Los cuadritos");
        guide.setPlaces(new Place[]{p1, p2, p3});
        return guide;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);


        TextToSpeechSingleton.getInstance().init(getApplicationContext());
        if (TextToSpeechSingleton.getInstance().getGuide() == null) {
            TextToSpeechSingleton.getInstance().setGuide(getGuide());
        }


        FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener(this);

        Fragment locationfragment = fm.findFragmentByTag(LocationFragment.TAG);
        if (locationfragment == null) {
            locationfragment = new LocationFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.guiderootview, locationfragment, LocationFragment.TAG);
            ft.commit();
        }

        updateBreadCrumbs();

//        Intent checkIntent = new Intent();
//        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//        startActivityForResult(checkIntent, TTS_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == TTS_REQUEST_CODE) {
//            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
//                // success, create the TTS instance
//                tts = new TextToSpeech(this, this);
//            } else {
//
////                // missing data, install it
////                Intent installIntent = new Intent();
////                installIntent.setAction(
////                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
////                startActivity(installIntent);
//                tts = null;
//            }
//        }
//    }

    private void updateBreadCrumbs() {

        Guide guide = TextToSpeechSingleton.getInstance().getGuide();
        int chapter = TextToSpeechSingleton.getInstance().getChapter();
        if (guide == null) {
            getActionBar().setTitle(getResources().getText(R.string.title_activity_read_guide));
        } else {
            getActionBar().setTitle(guide.getTitle());
        }
    }

    @Override
    public void onBackStackChanged() {
        updateBreadCrumbs();
    }
}
