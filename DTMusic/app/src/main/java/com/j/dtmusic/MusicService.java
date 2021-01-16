package com.j.dtmusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.MediaController;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    int songPos = 0;

    ArrayList<Song> songs = new ArrayList<>();
    MediaPlayer mediaPlayer;
    BroadcastReceiver mReceiver;

    private final IBinder musicBinder = new MusicBinder();

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public MusicService() {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Log.d("AAA", "onPrepared: " + mediaPlayer.isPlaying());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        Log.d("AAA", "MS onCreate");
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(MainActivity.ACTION_PLAY);
//        mReceiver = new MyReceiver();
//        registerReceiver(mReceiver, filter);
    }

//    private class MyReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(MainActivity.ACTION_PLAY)) {
//                setSongPos(intent.getIntExtra(MainActivity.INDEX_MP3, 0));
//                playSong();
//            }
//        }
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AAA", "MS onDestroy");
    }

    public int getPos() {
        return songPos;
    }

    public int getCurPos() {
        return mediaPlayer.getCurrentPosition();
    }

    public boolean isPng() {
        return mediaPlayer.isPlaying();
    }

    public void go() {
        mediaPlayer.start();
    }

    public void pausePlayer() {
        mediaPlayer.pause();
    }

    public void stp() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(songs.get(songPos).getPath()));
            songPos--;
            Log.d("AAA", "MS stp: " + songPos);
        } catch (IllegalStateException | IOException e) {
            Log.d("AAA", e.getMessage());
        }
    }

    public void seek(int pos) {
        mediaPlayer.seekTo(pos);
    }

    public void setList(ArrayList<Song> songs) {
        this.songs = songs;
        Log.d("AAA", "MS setList: " + songs.get(0).getTitle());
    }

    public void setSongPos(int songPos) {
        this.songPos = songPos;
        Log.d("AAA", "MS setSongPos = " + songPos + ", this.songPos = " + this.songPos);
    }

    public void playNext() {
        if (songPos < (songs.size() - 1)) songPos++;
        else songPos = 0;
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        playSong();
    }

    public void playPrev() {
        if (songPos > 0) songPos--;
        else songPos = songs.size() - 1;
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        playSong();
    }

    public void playSong() {
        mediaPlayer.reset();
        Log.d("AAA", "MS playSong: songPos = " + songPos);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(songs.get(songPos).getPath()));
            mediaPlayer.prepareAsync();
            createNotification();
            Log.d("AAA", "MS playSong: " + songPos + ", " + songs.get(songPos).getTitle());
        } catch (IllegalStateException | IOException e) {
            Log.d("AAA", e.getMessage());
        }

    }

    private void createNotification() {
        Intent notifIntent = new Intent(this, PlayAcitivity.class);

        notifIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        PendingIntent pendingIntent = PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{notifIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d("AAA", "createNotification");
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(notifIntent);
//        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        final int NOTIFY_ID = 1;
        final String CHANNEL_ID = "My Channel";
        final String CHANNEL_NAME = "My Background Service";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
//        Bitmap imageBitmap = Bitmap.createBitmap(new int[]{R.drawable.music_notif});
        builder.setContentIntent(pendingIntent)
                .setTicker(songs.get(songPos).getTitle())
                .setSmallIcon(R.drawable.music_notif)
                .setBadgeIconType(R.drawable.music_notif)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songs.get(songPos).getTitle());

        NotificationManager manager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        // Create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(NOTIFY_ID, builder.build());
    }

}
