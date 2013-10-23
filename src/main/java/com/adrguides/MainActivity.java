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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


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
        listguidebooks.add(new GuideBookItem("", "Null guidebook", Locale.UK.getDisplayName(), null));

        Collections.sort(listguidebooks);

        aa.addAll(listguidebooks);

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
}
