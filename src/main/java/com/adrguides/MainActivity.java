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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {

    private ActionMode actionmodeForList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final ListView list = (ListView) findViewById(R.id.listGuideBooks) ;

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (actionmodeForList == null) {
                    GuideBookItem o = (GuideBookItem)list.getAdapter().getItem(i);
                    Intent intent = new Intent(MainActivity.this, ReadGuideActivity.class);
                    intent.setData(Uri.parse(o.getURI()));
                    intent.putExtra(ReadGuideActivity.ARG_GUIDE_TITLE, o.getTitle());
                    startActivity(intent);
                    Log.d("com.adrguides.MainActivity", "list clicked null");
                    list.setItemChecked(i, false);
                } else {
                    Log.d("com.adrguides.MainActivity", "list clicked not null");

                    int ischecked = list.getCheckedItemPosition();
                    if (ischecked < 0) {
                        actionmodeForList.finish();
                    } else {
                        actionmodeForList.setTitle(list.getAdapter().getItem(i).toString());
                        // list.setItemChecked(i, true); // already checked
                    }
                }
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (actionmodeForList == null) {
                    list.setItemChecked(i, true);
                    // Start Action Mode.
                    actionmodeForList = startActionMode(actionmodeCallbackForList);
                    actionmodeForList.setTitle(list.getAdapter().getItem(i).toString());
                    return true;
                } else {
                    Log.d("com.adrguides.MainActivity", "longclicked");
                    return false; // we want the click to be launched also after long clicked.
                }
            }
        });

        GuideBookItemAdapter aa = new GuideBookItemAdapter(this);
        list.setAdapter(aa);

        List<GuideBookItem> listguidebooks = new ArrayList<GuideBookItem>();
        File[] bmps = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return file.isDirectory() && s.startsWith("saved-");
            }
        });
        for (File b: bmps) {

            try {
                listguidebooks.add(new GuideBookItem(
                        new File(b, "guidebook.json").toURI().toString(),
                        readFileText(new File(b, "guidebook.title.txt")),
                        readFileText(new File(b, "guidebook.locale.txt")),
                        new File(b, "guidebook.image.png").toString()));
                Log.d("com.adrguides.MainActivity", "imageurl " + new File(b, "guidebook.image.png").toURI().toString());

            } catch (IOException e) {
                Log.d("com.adrguides.MainActivity", "Directory is not a guidebook: " + b.getPath());
            }
        }

        // Internal test guidebooks.
        listguidebooks.add(new GuideBookItem("file:///android_asset/mockguide.json", "Mock guidebook", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook", Locale.US.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 2", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 3", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 4", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 5", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 6", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 7", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 8", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 9", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 10", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 11", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 12", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 13", Locale.UK.getDisplayName(), null));
        listguidebooks.add(new GuideBookItem("", "Null guidebook 14", Locale.UK.getDisplayName(), null));

        Collections.sort(listguidebooks);

        aa.addAll(listguidebooks);
    }
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
        Log.d("com.adrguides.MainActivity", "Start Checked: " + ((ListView) findViewById(R.id.listGuideBooks)).getCheckedItemPosition());
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").

        // it is selected??
        ListView l = (ListView) findViewById(R.id.listGuideBooks);
        if (actionmodeForList == null && l.getCheckedItemPosition() >= 0) {
            // Start Action Mode.
            actionmodeForList = startActionMode(actionmodeCallbackForList);
            actionmodeForList.setTitle(l.getAdapter().getItem(l.getCheckedItemPosition()).toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
        Log.d("com.adrguides.MainActivity", "Pause Checked: " + (actionmodeForList == null));


    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
        Log.d("com.adrguides.MainActivity", "Pause Checked: " + (actionmodeForList == null));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        Log.d("com.adrguides.MainActivity", "Pause Checked: " + (actionmodeForList == null));
    }
    private String readFileText(File file) throws IOException {
        Reader filename = null;
        try {
            filename = new InputStreamReader(new FileInputStream(file), "UTF-8");
            char[] buffer = new char[1024];
            int len;
            StringBuffer text = new StringBuffer();
            while ((len = filename.read(buffer)) != -1) {
                text.append(buffer, 0, len);
            }
            return text.toString();
        } finally {
            if (filename != null) {
                try {
                    filename.close();
                } catch (IOException e) {
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onSettingsClicked(MenuItem item) {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onAboutClicked(MenuItem item) {

        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private ActionMode.Callback actionmodeCallbackForList = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_context, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_openguidebook:

                    //shareCurrentItem();

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_deleteguidebook:

                    //shareCurrentItem();

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {

            // uncheck
            ListView l = (ListView) findViewById(R.id.listGuideBooks);
            l.setItemChecked(l.getCheckedItemPosition(), false);

            actionmodeForList = null;
        }
    };
}
