package com.j.dtmusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "DT_MUSIC";
    private static final String TABLE_NAME = "Song";

    private static final String COLUMN_ID = "Id";
    private static final String COLUMN_TITLE = "Title";
    private static final String COLUMN_ARTIST = "Artist";
    private static final String COLUMN_ALBUM = "Album";
    private static final String COLUMN_PATH = "Path";
    private static final String COLUMN_DURATION = "Duration";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_table = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TITLE + " TEXT," + COLUMN_ARTIST + " TEXT," + COLUMN_ALBUM + " TEXT,"
                + COLUMN_PATH + " TEXT," + COLUMN_DURATION + " TEXT)";
        db.execSQL(create_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addSong(Song song) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, song.getTitle());
        values.put(COLUMN_ARTIST, song.getArtist());
        values.put(COLUMN_ALBUM, song.getAlbum());
        values.put(COLUMN_PATH, song.getPath());
        values.put(COLUMN_DURATION, song.getDuration());
        database.insert(TABLE_NAME, null, values);
        database.close();
    }

    public void deleteSong(Song song) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(song.getId())});
    }

    public ArrayList<Song> getAllSongs() {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<Song> list = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Song(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getInt(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getSongCount() {
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
