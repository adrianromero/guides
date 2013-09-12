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

import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;


/**
 * Created by adrian on 19/08/13.
 */
public class ReadGuideFragment extends Fragment implements TTSFragment.PlayingListener {

    public final static String TAG = "LOCATION_FRAGMENT";

    private View v;
    private SearchViewGuides searchview;

    private TTSFragment ttsfragment;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_location, container, false);

        ImageSwitcher imageSwitcher = (ImageSwitcher) v.findViewById(R.id.switcherImageGuide);
        imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
        imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView iView = new ImageView(getActivity());
                iView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iView.setCropToPadding(false);
                iView.setLayoutParams(new ImageSwitcher.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                iView.setBackgroundColor(0xFF000000);
                return iView;
            }
        });

        ((TextView) v.findViewById(R.id.textContent)).setMovementMethod(new ScrollingMovementMethod());

        return v;
    }

    public void onStart () {
        super.onStart();

        ttsfragment = (TTSFragment) getFragmentManager().findFragmentByTag(TTSFragment.TAG);
        ttsfragment.setPlayingListener(this);
        printStatus();
    }
    public void onStop () {
        ttsfragment.setPlayingListener(null);
        ttsfragment = null;
        super.onStop();
    }

    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.read_guide, menu);

        menu.findItem(R.id.action_playpause).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ttsfragment.playstartpause();
                return true;
            }
        });

        menu.findItem(R.id.action_begin).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ttsfragment.restartChapter();
                return true;
            }
        });

        menu.findItem(R.id.action_next).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ttsfragment.gotoNext();
                return true;
            }
        });

        menu.findItem(R.id.action_first).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ttsfragment.gotoFirst();
                return true;
            }
        });
        menu.findItem(R.id.action_previous).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ttsfragment.gotoPrevious();
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

        searchview = new SearchViewGuides(getActivity(), menu.findItem(R.id.menu_search));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (ttsfragment.isTTSReady() && ttsfragment.isGuideAvailable() && ttsfragment.isGuideLanguageAvailable()) {

            boolean playing = ttsfragment.isPlaying();

            menu.findItem(R.id.action_playpause)
                    .setEnabled(true)
                    .setTitle(playing ? R.string.action_pause : R.string.action_play)
                    .setIcon(playing ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
            menu.findItem(R.id.menu_search)
                    .setEnabled(true)
                    .setIcon(R.drawable.ic_menu_search);
            menu.findItem(R.id.action_begin).setEnabled(true);
            menu.findItem(R.id.action_next).setEnabled(true);
            menu.findItem(R.id.action_first).setEnabled(true);
            menu.findItem(R.id.action_previous).setEnabled(true);
            menu.findItem(R.id.action_list).setEnabled(true);
        } else {
            menu.findItem(R.id.action_playpause)
                    .setEnabled(false)
                    .setIcon(getDrawableDisabled(R.drawable.ic_media_play));
            menu.findItem(R.id.menu_search)
                    .setEnabled(false)
                    .setIcon(getDrawableDisabled(R.drawable.ic_menu_search));
            menu.findItem(R.id.action_begin).setEnabled(false);
            menu.findItem(R.id.action_next).setEnabled(false);
            menu.findItem(R.id.action_first).setEnabled(false);
            menu.findItem(R.id.action_previous).setEnabled(false);
            menu.findItem(R.id.action_list).setEnabled(false);
        }
    }

    private Drawable getDrawableDisabled(int res) {
        Drawable resIcon = getResources().getDrawable(res);
        resIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return resIcon;
    }

    private void printStatus() {

        TextView title = (TextView) v.findViewById(R.id.textTitle);
        TextView content = (TextView) v.findViewById(R.id.textContent);
        TextView message = (TextView) v.findViewById(R.id.textMessage);
        ProgressBar progress = (ProgressBar) v.findViewById(R.id.progressMessage);

        if (ttsfragment.isTTSReady() && ttsfragment.isGuideAvailable() && ttsfragment.isGuideLanguageAvailable()) {

            Guide guide = ttsfragment.getGuide();
            int chapter = ttsfragment.getChapter();
            int paragraph = ttsfragment.getParagraph();

            message.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            Place mychapter = guide.getPlaces()[chapter];
            title.setVisibility(View.VISIBLE);
            title.setText(mychapter.getVisibleLabel());
            if (paragraph >= 0) {
                content.setVisibility(View.VISIBLE);
                content.setText(mychapter.getSections()[paragraph].getText());
                Bitmap b = mychapter.getSections()[paragraph].getImage();
                if (b == null) {
                    b = mychapter.getImage();
                }
                switchImage(b);
            } else {
                content.setVisibility(View.GONE);
                Bitmap b = mychapter.getImage();
                if (b == null && mychapter.getSections().length > 0) {
                    b = mychapter.getSections()[0].getImage();
                }
                switchImage(b);
            }
        } else {
            title.setVisibility(View.GONE);
            content.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
            switchImage(null);

            switchImage(null);
            if (!ttsfragment.isTTSReady()) {
                // Language not available
                if (ttsfragment.isInitialized()) {
                    // error
                    message.setText(getResources().getString(R.string.msg_tts_not_available));
                    progress.setVisibility(View.GONE);
                } else {
                    // Initializing
                    message.setText(getResources().getString(R.string.msg_tts_initializing));
                    progress.setVisibility(View.VISIBLE);
                }
            } else if (!ttsfragment.isGuideAvailable()) {
                message.setText(getResources().getString(R.string.msg_guide_not_available));
                progress.setVisibility(View.GONE);
            } else { // !ttsfragment.isGuideLanguageAvailable
                message.setText(getResources().getString(R.string.msg_guide_language_not_available, ttsfragment.getGuide().getLocale().getDisplayName()));
                progress.setVisibility(View.GONE);
            }
        }

        getActivity().invalidateOptionsMenu();
    }

    private Bitmap currentImage = null;
    private boolean firsttime = true;
    private void switchImage(Bitmap image) {

        if (image == currentImage && !firsttime) {
            return;
        }
        firsttime = false;
        currentImage = image;

        ImageSwitcher imageSwitcher = (ImageSwitcher) v.findViewById(R.id.switcherImageGuide);
        if (currentImage == null) {
            imageSwitcher.setImageResource(R.drawable.place_default);
        } else {
            imageSwitcher.setImageDrawable(new BitmapDrawable(getResources(), currentImage));
        }
    }

    @Override
    public void update() {
        v.post(new Runnable(){
            public void run() {
                if (ttsfragment != null) { // Fragment not stopped
                    printStatus();
                }
            }
        });
    }
}
