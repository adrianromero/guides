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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final ListView list = (ListView) findViewById(R.id.listView) ;

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GuideBookItem o = (GuideBookItem)list.getAdapter().getItem(i);
                Intent intent = new Intent(MainActivity.this, ReadGuideActivity.class);
                intent.setData(Uri.parse(o.getURI()));
                intent.putExtra(ReadGuideActivity.ARG_GUIDE_TITLE, o.getTitle());
                startActivity(intent);
            }
        });

        ArrayAdapter<GuideBookItem> aa = new ArrayAdapter(this, R.layout.item_guide, R.id.textItemTitle);
        list.setAdapter(aa);


        File[] bmps = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return file.isDirectory() && s.startsWith("saved-");
            }
        });
        for (File b: bmps) {

            Reader filename = null;
            try {
                filename = new InputStreamReader(new FileInputStream(new File(b, "guidebook.title.txt")), "UTF-8");
                char[] buffer = new char[1024];
                int len;
                StringBuffer title = new StringBuffer();
                while ((len = filename.read(buffer)) != -1) {
                    title.append(buffer, 0, len);
                }
                aa.add(new GuideBookItem(new File(b, "guidebook.json").toURI().toString(), title.toString()));
            } catch (IOException e) {
                Log.d("com.adrguides.MainActivity", "Directory is not a guidebook: " + b.getPath());
            } finally {
                if (filename != null) {
                    try {
                        filename.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        // Internal test guidebooks.
        aa.add(new GuideBookItem("file:///android_asset/mockguide.json", "Mock guidebook"));
        aa.add(new GuideBookItem("", "Null guidebook"));

        // aa.addAll(new String[] {"1", "2", "2", "2", "2", "2", "2", "2", "2", "2", "2", "2"});
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
}
