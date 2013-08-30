package com.adrguides;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.tts.TextToSpeechSingleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by adrian on 19/08/13.
 */
public class LocationFragment extends Fragment implements TextToSpeechSingleton.PlayingListener {

    public final static String TAG = "LOCATION_FRAGMENT";

    private View v;
    private SearchViewGuides searchview;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        v = inflater.inflate(R.layout.fragment_location, container, false);

        return v;
    }

    public void onResume () {
        super.onResume();

        TextToSpeechSingleton.getInstance().setPlayingListener(this);
        printStatus();
    }
    public void onPause () {
        super.onPause();

        TextToSpeechSingleton.getInstance().setPlayingListener(null);
    }
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.read_guide, menu);

        menu.findItem(R.id.action_playpause).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                TextToSpeechSingleton.getInstance().playstartpause();
                return true;
            }
        });

        menu.findItem(R.id.action_next).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                TextToSpeechSingleton.getInstance().gotoNext();
                return true;
            }
        });

        menu.findItem(R.id.action_first).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                TextToSpeechSingleton.getInstance().gotoFirst();
                return true;
            }
        });
        menu.findItem(R.id.action_previous).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                TextToSpeechSingleton.getInstance().gotoPrevious();
                return true;
            }
        });
        menu.findItem(R.id.action_list).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new ListDialogFragment().show(getFragmentManager(), ListDialogFragment.TAG);
                return true;
            }
        });

        searchview = new SearchViewGuides(this.getActivity(), menu.findItem(R.id.menu_search));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean playing = TextToSpeechSingleton.getInstance().isPlaying();

        MenuItem playpause = menu.findItem(R.id.action_playpause);
        playpause.setTitle(playing ? R.string.action_pause : R.string.action_play);
        playpause.setIcon(playing ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
    }

    private void printStatus() {

        Guide guide = TextToSpeechSingleton.getInstance().getGuide();
        int chapter = TextToSpeechSingleton.getInstance().getChapter();
        int paragraph = TextToSpeechSingleton.getInstance().getParagraph();
        // boolean playing = TextToSpeechSingleton.getInstance().isPlaying();

        TextView title = ((TextView) v.findViewById(R.id.textTitle));
        TextView content = ((TextView) v.findViewById(R.id.textContent));
        if (guide == null || chapter >= guide.getPlaces().length) {
            title.setVisibility(View.GONE);
            content.setVisibility(View.GONE);
        } else {
            Place mychapter = guide.getPlaces()[chapter];
            title.setVisibility(View.VISIBLE);
            title.setText(mychapter.getVisibleLabel());
            if (paragraph >= 0) {
                content.setVisibility(View.VISIBLE);
                content.setText(mychapter.getText()[paragraph]);
            } else {
                content.setVisibility(View.GONE);
            }
        }

        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void update() {
        v.post(new Runnable(){
            public void run() {
                printStatus();
            }
        });
    }
}
