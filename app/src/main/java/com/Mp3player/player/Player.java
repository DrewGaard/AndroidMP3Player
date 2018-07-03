package com.Mp3player.player;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import android.database.Cursor;
import android.provider.MediaStore;

import com.example.drew.mp3player.MainActivity;
import com.example.drew.mp3player.R;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by Drew on 3/13/2017.
 */
public class Player extends ActionBarActivity implements View.OnClickListener{
    static MediaPlayer mp;
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    byte[] artbyte;

    ArrayList<File> mySongs;
    int position;
    Uri u, custom;
    Thread updateSeekBar;
    Handler mHandler = new Handler();

    SeekBar sb, volume;
    Button btPlay, btFF, btFB, btNxt, btPv, songbtn;
    ImageView art;
    TextView album, artist, genre, length;
    Switch loopSwitch;
    AudioManager am;

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        btPlay = (Button) findViewById(R.id.btPlay);
        btFF = (Button) findViewById(R.id.btFF);
        btFB = (Button) findViewById(R.id.btFB);
        btNxt = (Button) findViewById(R.id.btNxt);
        btPv = (Button) findViewById(R.id.btPv);
        art = (ImageView) findViewById(R.id.art);
        album = (TextView) findViewById(R.id.album);
        artist = (TextView) findViewById(R.id.artist);
        genre = (TextView) findViewById(R.id.genre);
        length = (TextView) findViewById(R.id.length);
        loopSwitch = (Switch) findViewById(R.id.loopSwitch);
        songbtn = (Button) findViewById(R.id.songbtn);


        btPlay.setOnClickListener(this);
        btFF.setOnClickListener(this);
        btFB.setOnClickListener(this);
        btNxt.setOnClickListener(this);
        btPv.setOnClickListener(this);
        loopSwitch.setOnClickListener(this);
        songbtn.setOnClickListener(this);


        volume = (SeekBar) findViewById(R.id.volumeBar);
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxV = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curV = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume.setMax(maxV);
        volume.setProgress(curV);
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seeked_progess;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        sb = (SeekBar) findViewById(R.id.seekBar);
        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mp.getDuration();
                int currentPosition = 0;
                while (currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mp.getCurrentPosition();
                        sb.setProgress(currentPosition);
                        currentPosition = 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    


        if (mp != null){
            try {
                mp.reset();
                mp.prepare();
                mp.stop();
                mp.release();
                mp=null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent i = getIntent();
        Bundle b = i.getExtras();


            mySongs = (ArrayList) b.getParcelableArrayList("songlist");
            position = b.getInt("pos", 0);


            u = Uri.parse(mySongs.get(position).toString());


            mp = MediaPlayer.create(getApplicationContext(), u);
            sb.setProgress(0);// To set initial progress, i.e zero in starting of the song
            sb.setMax(mp.getDuration());// To set the max progress, i.e duration of the song
            mp.start();
            updateSeekBar.start();


        mmr.setDataSource(u.toString());
        try{
            artbyte = mmr.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(artbyte, 0, artbyte.length);
            art.setImageBitmap(songImage);
        } catch (Exception e) {
            art.setBackgroundColor(Color.GRAY);
        }

        try{
            int duration = 0;
            String dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (dur != null)
            {
                duration = Integer.parseInt(dur);
            }
            duration = duration / 1000;
            long h = duration / 3600;
            long m = (duration - h * 3600) / 60;
            long s = duration - (h * 3600 + m * 60);
            if(h == 0 || m == 0)
            {
                length.setText(m + ":" + s);
            }
            //length.setText(h + ":" + m + ":" + s);
        } catch (Exception e) {
            length.setText("Unknown Length");
        }

        try{
            genre.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        } catch (Exception e) {
            genre.setText("Unknown Genre");
        }

        try{
            artist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        } catch (Exception e) {
            artist.setText("Unknown Artist");
        }

        try{
            album.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        } catch (Exception e) {
            album.setText("Unknown Album");
        }


        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seeked_progess;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seeked_progess = progress;

                if (fromUser) {

                    Runnable mRunnable = new Runnable() {

                        @Override
                        public void run() {
                            int min, sec;

                            if (mp != null /*Checking if the
                       music player is null or not otherwise it
                       may throw an exception*/) {
                                int mCurrentPosition = sb.getProgress();

                                min = mCurrentPosition / 60;
                                sec = mCurrentPosition % 60;

                                Log.e("Music Player Activity", "Minutes : "+min +" Seconds : " + sec);

                        /*currentime_mm.setText("" + min);
                        currentime_ss.setText("" + sec);*/
                            }
                            mHandler.postDelayed(this, 1000);
                        }
                    };
                    mRunnable.run();}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            int index = volume.getProgress();
            volume.setProgress(index + 1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            int index = volume.getProgress();
            volume.setProgress(index - 1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btPlay:
                if(mp.isPlaying()) {
                    btPlay.setText(">");
                    mp.pause();
                }
                else {
                    btPlay.setText("||");
                    mp.start();
                }
                break;
            case R.id.btFF:
                mp.seekTo(mp.getCurrentPosition()+5000);
                break;
            case R.id.btFB:
                mp.seekTo(mp.getCurrentPosition()-5000);
                break;
            case R.id.btNxt:
                try {
                    mp.reset();
                    mp.prepare();
                    mp.stop();
                    mp.release();
                    mp=null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                position = (position+1)%mySongs.size();
                u = Uri.parse(mySongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                getSongInfo();
                btPlay.setText("||");
                sb.setProgress(0);// To set initial progress, i.e zero in starting of the song
                sb.setMax(mp.getDuration());// To set the max progress, i.e duration of the song
                mp.start();
                break;
            case R.id.btPv:
                try {
                    mp.reset();
                    mp.prepare();
                    mp.stop();
                    mp.release();
                    mp=null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                position = (position-1<0)? mySongs.size()-1: position-1;
                /*if(position-1 < 0)
                {
                    position = mySongs.size()-1;
                }
                else
                {
                    position = position - 1;
                }*/
                u = Uri.parse(mySongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(), u);
                getSongInfo();
                btPlay.setText("||");
                mp.start();
                sb.setProgress(0);// To set initial progress, i.e zero in starting of the song
                sb.setMax(mp.getDuration());// To set the max progress, i.e duration of the song
                break;
            case R.id.songbtn:
                finish();
                break;
            case R.id.loopSwitch:
                if(loopSwitch.isChecked())
                {
                    mp.setLooping(true);
                    if(mp.getCurrentPosition() == mp.getDuration()) {
                        mp.start();
                    }
                }
                else
                {
                    mp.setLooping(false);
                }
        }
    }



    public void getSongInfo()
    {
        mmr.setDataSource(u.toString());
        try{
            artbyte = mmr.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(artbyte, 0, artbyte.length);
            art.setImageBitmap(songImage);
            art.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) {
            art.setImageBitmap(null);
            art.setBackgroundColor(Color.GRAY);
        }

        try{
            int duration = 0;
            String dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (dur != null)
            {
                duration = Integer.parseInt(dur);
            }
            duration = duration / 1000;
            long h = duration / 3600;
            long m = (duration - h * 3600) / 60;
            long s = duration - (h * 3600 + m * 60);
            if(h == 0 || m == 0)
            {
                length.setText(m + ":" + s);
            }
            //length.setText(h + ":" + m + ":" + s);
        } catch (Exception e) {
            length.setText("Unknown Length");
        }

        try{
            genre.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        } catch (Exception e) {
            genre.setText("Unknown Genre");
        }

        try{
            artist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        } catch (Exception e) {
            artist.setText("Unknown Artist");
        }

        try{
            album.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        } catch (Exception e) {
            album.setText("Unknown Album");
        }
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

    public ArrayList<String> getAlbum() {
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
            Log.d("Arrayl", mArrayList1.toString());
        }

        return mArrayList1;
    }

}