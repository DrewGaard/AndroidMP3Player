package com.example.drew.mp3player;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.Mp3player.player.Player;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    ListView lv;
    Button albums, nowply;
    String[] items;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.lvPlaylist);

        albums = (Button) findViewById(R.id.albums);
        albums.setOnClickListener(this);

        nowply = (Button) findViewById(R.id.nowply);
        nowply.setOnClickListener(this);


        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        final ArrayList<File> mySongs = findSongs(Environment.getExternalStorageDirectory());
        items = new String[mySongs.size()];
        for (int i = 0; i < mySongs.size(); i++) {
            //toast(mySongs.get(i).getName().toString());
            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
        }

        ArrayAdapter<String> adp = new ArrayAdapter<String>(getApplicationContext(), R.layout.song_layout, R.id.textView, items);
        lv.setAdapter(adp);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(), Player.class).putExtra("pos", position).putExtra("songlist", mySongs));
            }
        });

    }


    public ArrayList<File> findSongs(File root) {
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                al.addAll(findSongs(singleFile));
            } else {
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                    al.add(singleFile);
                }
            }
        }
        return al;
    }

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.albums:
                ArrayAdapter<String> adp = new ArrayAdapter<String>(getApplicationContext(), R.layout.song_layout, R.id.textView, getAlbum());
                lv.setAdapter(adp);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        startActivity(new Intent(getApplicationContext(), Player.class).putExtra("pos", position).putExtra("songlist", getURI()));
                    }
                });
                break;
            case R.id.nowply:
                Intent intent = new Intent(MainActivity.this, Player.class);
                startActivity(intent);
                break;

        }
    }

    public ArrayList<String> getAlbum() {
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Media.TITLE);

        ArrayList<String> mArrayList = new ArrayList<String>();
        ArrayList<String> mArrayList1 = new ArrayList<String>();

        while (cursor.moveToNext()) {
            String Year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
            String Album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            String Duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String columnIndex = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            mArrayList.add(Album);
            Log.d("Array", mArrayList.toString());
        }

        return mArrayList;
    }

    public ArrayList<String> getURI() {
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Media.TITLE);

        ArrayList<String> mArrayList = new ArrayList<String>();
        ArrayList<String> mArrayList1 = new ArrayList<String>();

        while (cursor.moveToNext()) {
            String Year = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));
            String Album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            String Duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String columnIndex = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            mArrayList1.add(columnIndex);
            Log.d("Array1", mArrayList1.toString());
        }

        return mArrayList1;
    }
}
