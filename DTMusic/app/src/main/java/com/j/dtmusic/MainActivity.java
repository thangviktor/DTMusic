package com.j.dtmusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

//    SharedPreferences sharedPreferences;
//    SharedPreferences.Editor editor;

    public Database database;
    public ArrayList<Song> songs = new ArrayList<>();
    private ArrayList<Song> othersAudio = new ArrayList<>();
    private AdapterSong adapterSong;
    private ListView listView;

    public MusicService musicService;
    private Intent playIntent;
    public boolean binded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listMusic);

        checkAndRequestPermissions();
        database = new Database(this);
        songs = database.getAllSongs();
        if (songs.isEmpty()) getMusicFile();

        adapterSong = new AdapterSong(this, R.layout.listview_music, songs);
        listView.setAdapter(adapterSong);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicService.setSongPos(position);
                musicService.playSong();
                Log.d("AAA", "Main: " + songs.get(position).getTitle());

                Intent intent = new Intent(view.getContext(), PlayAcitivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        Log.d("AAA", "onCreate");
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ArrayList<String> list = new ArrayList<>();
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                    list.add(permission);
            }
            if (!list.isEmpty())
                requestPermissions(list.toArray(new String[list.size()]), RESULT_FIRST_USER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.playmusic) {
            Intent intentPA = new Intent(MainActivity.this, PlayAcitivity.class);
            startActivity(intentPA);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMusicFile() {
        database = new Database(MainActivity.this);

        ContentResolver resolver = getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.TITLE + " ASC");
        if (cursor.moveToFirst()) {
            do {
                long idFile = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, idFile);
                Song newSong = new Song();
                newSong.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                newSong.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                newSong.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                newSong.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                newSong.setPath(uri.toString());
//                long albumId = Long.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
//                Cursor cursorAlbum = getApplicationContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
//                        new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
//                        MediaStore.Audio.Albums._ID + "=?", new String[] {String.valueOf(albumId)}, null);
//
//                if(cursorAlbum != null && cursorAlbum.moveToFirst()){
//                    String albumCoverPath = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
//
//                    if (albumCoverPath != null)
//                    try {
//                        Bitmap bmthumb = getContentResolver().loadThumbnail(Uri.parse(albumCoverPath), new Size(500, 500), null);
//                        newSong.setBmthumb(bmthumb);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d("AAA", newSong.getTitle() + " | " + albumCoverPath);
//                }
                database.addSong(newSong);

            } while (cursor.moveToNext());
        }
        // Remove other Audio files
        songs = database.getAllSongs();
        if (!songs.isEmpty()) {
                int i = 0;
                String[] albumToRemove = {"call_rec", "sounds", "soundConfig", "Notifications", "Ringtones"};
                while (i  < songs.size()) {
                    String album = songs.get(i).getAlbum();
                    boolean checkRemove = false;
                    for (int j = 0; j < albumToRemove.length; j++)
                        if (album.equals(albumToRemove[j])) {
                            checkRemove = true;
                            othersAudio.add(songs.get(i));
                            database.deleteSong(songs.get(i));
                            songs.remove(i);
                        }
                    if (!checkRemove) i++;
                }
        }

    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) service;
            musicService = musicBinder.getService();
            musicService.setList(songs);
            binded = true;

            Log.d("AAA", "Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binded = false;
            Log.d("AAA", "Connect fail");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            Log.d("AAA", "playIntent == null");
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, BIND_AUTO_CREATE);
            startService(playIntent);
        } else Log.d("AAA", "playIntent != null");

        Log.d("AAA", "onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);
        stopService(playIntent);
    }
}