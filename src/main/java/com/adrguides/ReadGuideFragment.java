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

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
// import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.adrguides.model.Guide;
import com.adrguides.model.Place;
import com.adrguides.utils.HTTPUtils;

//import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Target;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by adrian on 19/08/13.
 */
public class ReadGuideFragment extends Fragment implements TTSFragment.PlayingListener {

    public final static String TAG = "LOCATION_FRAGMENT";

    private final static String IMAGE_BLANK = "<<IMAGE_BLANK>>";

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
                PhotoView iView = new PhotoView(getActivity());
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

//        Picasso.with(this.getActivity()).cancelRequest(target);
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

        menu.findItem(R.id.action_next).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ttsfragment.gotoNext();
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

        menu.findItem(R.id.action_viewguide).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

//                Intent internetIntent = new Intent(Intent.ACTION_VIEW);
//                internetIntent.setData(Uri.parse(ttsfragment.getGuide().getAddress()));
//                startActivity(internetIntent);

                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, ttsfragment.getGuide().getAddress());
                intent.putExtra(WebViewActivity.EXTRA_TITLE, ttsfragment.getGuide().getTitle());
                startActivity(intent);

                return true;
            }
        });

        menu.findItem(R.id.action_bookmark).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                new SaveGuideTask(ReadGuideFragment.this.getActivity().getApplicationContext())
                        .execute(ttsfragment.getGuide());
                return true;
            }
        });

        searchview = new SearchViewGuides(getActivity(), menu.findItem(R.id.menu_search));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_viewguide).setEnabled(
                ttsfragment.getGuide() != null && ttsfragment.getGuide().getAddress() != null);

        if (ttsfragment.isTTSReady() && ttsfragment.isGuideAvailable() && ttsfragment.isGuideLanguageAvailable()) {

            boolean playing = ttsfragment.isPlaying();

            menu.findItem(R.id.action_playpause)
                    .setEnabled(true)
                    .setTitle(playing ? R.string.action_stop : R.string.action_play)
                    .setIcon(playing ? R.drawable.ic_media_stop : R.drawable.ic_media_play);
            menu.findItem(R.id.menu_search)
                    .setEnabled(true)
                    .setIcon(R.drawable.ic_menu_search);
            if (ttsfragment.isEnabledNext()) {
                menu.findItem(R.id.action_next)
                        .setEnabled(true)
                        .setIcon(R.drawable.ic_media_next);
            } else {
                menu.findItem(R.id.action_next)
                        .setEnabled(false)
                        .setIcon(getDrawableDisabled(R.drawable.ic_media_next));
            }
            if (ttsfragment.isEnabledPrevious()) {
                menu.findItem(R.id.action_previous)
                        .setEnabled(true)
                        .setIcon(R.drawable.ic_media_previous);
            } else {
                menu.findItem(R.id.action_previous)
                        .setEnabled(false)
                        .setIcon(getDrawableDisabled(R.drawable.ic_media_previous));
            }
            menu.findItem(R.id.action_bookmark).setEnabled(!ttsfragment.getGuide().isStored());
            menu.findItem(R.id.action_list).setEnabled(true);
        } else {
            menu.findItem(R.id.action_playpause)
                    .setEnabled(false)
                    .setIcon(getDrawableDisabled(R.drawable.ic_media_play));
            menu.findItem(R.id.menu_search)
                    .setEnabled(false)
                    .setIcon(getDrawableDisabled(R.drawable.ic_menu_search));
            menu.findItem(R.id.action_next)
                    .setEnabled(false)
                    .setIcon(getDrawableDisabled(R.drawable.ic_media_next));
            menu.findItem(R.id.action_previous)
                    .setEnabled(false)
                    .setIcon(getDrawableDisabled(R.drawable.ic_media_previous));
            menu.findItem(R.id.action_bookmark).setEnabled(false);
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
            String imageURL;

            // Print title in bar
            getActivity().getActionBar().setTitle(guide.getTitle());

            message.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            Place mychapter = guide.getPlaces().get(chapter);
            title.setVisibility(View.VISIBLE);
            title.setText(mychapter.getVisibleLabel());
            if (paragraph >= 0) {
                content.setVisibility(View.VISIBLE);
                content.setText(mychapter.getSections().get(paragraph).getText());
                imageURL = mychapter.getSections().get(paragraph).getImageURL();
            } else {
                content.setVisibility(View.GONE);
                if (mychapter.getSections().size() > 0) {
                    imageURL = mychapter.getSections().get(0).getImageURL();
                } else {
                    imageURL = null;
                }
            }
            switchImage(imageURL);
        } else {

            getActivity().getActionBar().setTitle(getResources().getText(R.string.title_activity_read_guide));

            title.setVisibility(View.GONE);
            content.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);

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

    private String currentImageURL = null; // not already asigned an image
    private SwitchImageTask t = null;

    // This address is supposed to be always a file://...
    private void switchImage(String imageURL) {

        if (imageURL == null) {
            imageURL = IMAGE_BLANK;
        }

        if (imageURL.equals(currentImageURL)) {
            // do not switch if the image is the same
            return;
        }

        if (t != null) {
            t.cancel(true);
            t = null;
        }

        Log.d("com.adrguides.ReadGuideFragment", "Switching to " + imageURL);

        currentImageURL = imageURL;

        ImageSwitcher imageSwitcher = (ImageSwitcher) v.findViewById(R.id.switcherImageGuide);
        if (currentImageURL.equals(IMAGE_BLANK)) {
            imageSwitcher.setImageResource(R.drawable.place_default);
        } else {
//            Picasso.with(this.getActivity()).load(imageURL).into(target);
            t = new SwitchImageTask();
            t.execute(this.getActivity().getApplicationContext(), currentImageURL);
        }
    }

//    private Target target = new Target() {
//        @Override
//        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//            if (ttsfragment != null ) { // Fragment not stopped
//                ((ImageSwitcher) v.findViewById(R.id.switcherImageGuide)).setImageDrawable(new BitmapDrawable(getResources(), bitmap));
//            }
//        }
//        @Override
//        public void onBitmapFailed(Drawable drawable) {
//            if (ttsfragment != null ) { // Fragment not stopped
//                ((ImageSwitcher) v.findViewById(R.id.switcherImageGuide)).setImageResource(R.drawable.place_default);
//            }
//        }
//        @Override
//        public void onPrepareLoad(Drawable drawable) {
//        }
//    };

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

    private class SwitchImageTask extends AsyncTask<Object, Void, Drawable>{

        @Override
        protected Drawable doInBackground(Object... params) {

            Context appcontext = (Context) params[0];
            String address = (String) params[1];

            // if file does not exist bitmap will be null and drawable will be a black rectangle.
            // that is OK for me.
            InputStream inimage = null;
            try {
                inimage = HTTPUtils.openAddress(appcontext, new URL(address));
                return new BitmapDrawable(getResources(), inimage);
            } catch (IOException e) {
                Log.d("com.adrguides.SwitchImageTask", e.getMessage());
                return null;
            } finally {
                if (inimage != null) {
                    try {
                        inimage.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (ttsfragment != null ) { // Fragment not stopped
                ((ImageSwitcher) v.findViewById(R.id.switcherImageGuide)).setImageDrawable(result);
            }
        }
    }

    private class SaveGuideTask extends AsyncTask<Guide, Void, Boolean> {
        private Context appcontext;

        public SaveGuideTask(Context appcontext) {
            this.appcontext = appcontext;
        }

        protected Boolean doInBackground(Guide... params) {

            try {
                Guide guide = params[0];
                guide.saveToDisk(appcontext);
                return true;
            } catch (IOException ex) {
                return false;
            } catch (JSONException ex) {
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {

            CharSequence text = appcontext.getString(result
                    ? R.string.msg_bookmark_saved
                    : R.string.msg_bookmark_not_saved);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(appcontext, text, duration);
            toast.show();
            update();
        }
    }
}
