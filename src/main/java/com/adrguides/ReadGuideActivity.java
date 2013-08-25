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


public class ReadGuideActivity extends Activity implements LocationHandler, FragmentManager.OnBackStackChangedListener {

    private static final int TTS_REQUEST_CODE = 332342;

    public static final String ARG_GUIDE = "ARG_GUIDE";
    public static final String ARG_PLACE = "ARG_PLACE";

    private Guide guide;

    private static Guide getGuide() {

        Place p1 = new Place();
        p1.setTitle("Cuadro primero");
        p1.setText(new String[] {
                "Este es el cuadro primero. \nNo es el mejor de todos pero al menos va el primero y eso se nota. Y eso es algo que no todo el mundo sabe apreciar.",
                "Muy bonito.",
                "Y me gusta mucho"
        });

        Place p2 = new Place();
        p2.setTitle("Cuadro segundo");
        p2.setText(new String[] {
                "Este es el cuadro segundo.",
                "Muy abstracto.",
                "Y me gusta menos que el primero y más que el segundo."
        });


        Place p3 = new Place();
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

        guide = getGuide();

        FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener(this);

        Fragment guidefragment = fm.findFragmentByTag(GuideFragment.TAG);
        if (guidefragment == null) {
            guidefragment = new GuideFragment();
            Bundle b = new Bundle();
            b.putParcelable(ReadGuideActivity.ARG_GUIDE, guide);
            guidefragment.setArguments(b);

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.guiderootview, guidefragment, GuideFragment.TAG);
            ft.commit();
        }

        updateBreadCrumbs();

        TextToSpeechSingleton.getInstance().init(getApplicationContext());

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.read_guide, menu);
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
        getActionBar().setTitle(guide.getTitle());
        int entries = getFragmentManager().getBackStackEntryCount();
        if (entries == 0) {
            getActionBar().setSubtitle(" ");
        } else {
            getActionBar().setSubtitle(getFragmentManager().getBackStackEntryAt(entries - 1).getBreadCrumbTitle());
        }
    }

    @Override
    public void showPlace(Place place) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment locationfragment = new LocationFragment();
        Bundle b = new Bundle();
        b.putParcelable(ReadGuideActivity.ARG_PLACE, place);
        locationfragment.setArguments(b);

        ft.add(R.id.guiderootview, locationfragment, LocationFragment.TAG);
        ft.addToBackStack(null);
        // ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        ft.setBreadCrumbTitle(place.getTitle());
        ft.commit();
    }


    @Override
    public void onBackStackChanged() {
        updateBreadCrumbs();
        TextToSpeechSingleton.getInstance().getTTS().stop();
    }
}
