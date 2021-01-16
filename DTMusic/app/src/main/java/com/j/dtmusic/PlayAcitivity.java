package com.j.dtmusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PlayAcitivity extends AppCompatActivity {

    ImageButton play, stop, next, previous;
    ImageView disc;
    TextView songName, time, totalTime;
    SeekBar seekBar;

    private int position;
    private int loopType = 0;
    private Handler handler;

    private MusicService musicService;
    private Intent playIntent;
    private boolean binded = false;
    public boolean stopplayed = false;

//    SharedPreferences sharedPreferences;
//    SharedPreferences.Editor editor;

    ArrayList<Song> songs = new ArrayList<>();
    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        play        = findViewById(R.id.musicPlay);
        stop        = findViewById(R.id.musicStop);
        next        = findViewById(R.id.musicNext);
        previous    = findViewById(R.id.musicPrevious);
        songName    = findViewById(R.id.musicSongName);
        time        = findViewById(R.id.musicTime);
        totalTime   = findViewById(R.id.musicTotalTime);
        seekBar     = findViewById(R.id.musicSeekBar);
        disc        = findViewById(R.id.discImage);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);

        database = new Database(this);
        songs = database.getAllSongs();
        Log.d("AAA", "Play onCreate: " + position + ", " + songs.get(position).getTitle());

        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_disc);

//        sharedPreferences = getSharedPreferences("LoopType", MODE_PRIVATE);
//        loopType = sharedPreferences.getInt("loopType", 0);
//        position = sharedPreferences.getInt("position", 0);
        handler = new Handler();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (musicService != null && binded) {
                    if (musicService.isPng()) {
                        musicService.pausePlayer();
                        play.setImageResource(R.drawable.play_foreground);
                        disc.clearAnimation();
                    }
                    else {
                        musicService.go();
                        play.setImageResource(R.drawable.pause_foreground);
                        disc.startAnimation(animation);
                    }
                }

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService!= null && binded) {
                    musicService.stp();
                    play.setImageResource(R.drawable.play_foreground);
                    seekBar.setProgress(0);
                    time.setText("00:00");
                    disc.clearAnimation();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService!= null && binded) {
                    musicService.playNext();
                    disc.startAnimation(animation);
                    setNewSong();
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService!= null && binded) {
                    musicService.playPrev();
                    disc.startAnimation(animation);
                    setNewSong();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seek(seekBar.getProgress());
            }
        });
    }
    
    private void setNewSong() {
        Song curSong = songs.get(musicService.getPos());
        songName.setText(curSong.getTitle());
        SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
        totalTime.setText(timeFormat.format(curSong.getDuration()));
        seekBar.setMax(curSong.getDuration());
        play.setImageResource(R.drawable.pause_foreground);
    }

    private Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
            if (musicService != null && binded) {
                    time.setText(timeFormat.format(musicService.getCurPos()));
                    seekBar.setProgress(musicService.getCurPos());
//                }
            }
//
//            musicService.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    switch (loopType) {
//                        case 0:
//                            musicService.mediaPlayer.stop();
//                            if (position < (songs.size() - 1)) position++;
//                            else position = 0;
//                            SetNewSong(false);
//                            break;
//                        case 1:
//                            musicService.mediaPlayer.stop();
//                            musicService.mediaPlayer = MediaPlayer.create(PlayAcitivity.this, Uri.parse(songs.get(position).getPath()));
//                            seekBar.setProgress(0);
//                            musicService.mediaPlayer.start();
//                            break;
//                        case 2:
//                            musicService.mediaPlayer.stop();
//                            play.setImageResource(R.drawable.play_foreground);
//                            musicService.mediaPlayer = MediaPlayer.create(PlayAcitivity.this, Uri.parse(songs.get(position).getPath()));
//                            seekBar.setProgress(0);
//                            break;
//                    }
//                }
//            });
            handler.postDelayed(updateTime, 500);
        }
    };



    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) service;
            musicService = musicBinder.getService();
            setNewSong();
            updateTime.run();
            binded = true;

            Log.d("AAA", "Play Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binded = false;
            Log.d("AAA", "Play Connect fail");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, BIND_AUTO_CREATE);
            startService(playIntent);
        } else Log.d("AAA", "Play playIntent != null");

        Log.d("AAA", "Play onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
        stopService(playIntent);
        Log.d("AAA", "onDestroy");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intentMain = new Intent(this, MainActivity.class);
        startActivity(intentMain);
    }
}