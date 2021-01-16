package com.j.dtmusic;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterSong extends BaseAdapter {
    private Context context;
    private int layout;
    private ArrayList<Song> listSong;

    public AdapterSong(Activity context, int layout, ArrayList<Song> listSong) {
        this.context = context;
        this.layout = layout;
        this.listSong = listSong;
    }

    @Override
    public int getCount() {
        return listSong.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder {
//        ImageView thumbnail;
        TextView title, artist_album;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, null);

            holder = new ViewHolder();
            holder.artist_album = (TextView) convertView.findViewById(R.id.artist_albumView);
            holder.title = (TextView) convertView.findViewById(R.id.titleView);
            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag();

        Song song = listSong.get(position);
        holder.title.setText(song.getTitle());
        String ar_al = song.getArtist() + " | " + song.getAlbum();
        holder.artist_album.setText(ar_al);
//        if (song.getBmthumb() != null) {
//            Log.d("AAAA", "Ok");
//            holder.thumbnail.setImageBitmap(song.getBmthumb());
//        } else Log.d("AAAA", "NO thumbnail");

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_view);
        convertView.startAnimation(animation);
        return convertView;
    }
}
