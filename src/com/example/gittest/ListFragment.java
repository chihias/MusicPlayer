
package com.example.gittest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ListFragment extends Fragment {

    private Activity mActivity;
    @Override
    public void onAttach(Activity activity) {//mActivity要先initialize
        super.onAttach(activity);
        mActivity = activity;
    }

    private ArrayList<Song> mSongList;
    private ListView mSongView;

    public static final String PROVIDER_MEDIA_TITLE = "android.provider.MediaStore.Audio.Media.TITLE";
    public static final String PROVIDER_MEDIA_ID = "android.provider.MediaStore.Audio.Media._ID";
    public static final String PROVIDER_MEDIA_ARTIST = "android.provider.MediaStore.Audio.Media.ARTIST";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) { //在activity底下放在create的內容 在fragment要放在activitycreate
        super.onActivityCreated(savedInstanceState);
        setupViewComponent();
        instanList();
        getSongList(); // Call the helper method
        // sort songs by title
        Collections.sort(mSongList, new Comparator<Song>() {
            @Override
            public int compare(Song aSong, Song bSong) {
                return aSong.getTitle().compareTo(bSong.getTitle());
            }
        });
        SongAdapter songAdt = new SongAdapter(mActivity, mSongList);
        mSongView.setAdapter(songAdt);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void setupViewComponent() {
        mSongView = (ListView) getView().findViewById(R.id.song_list);
    }

    private void instanList() {
        mSongList = new ArrayList<Song>();
    }

    // a helper method
    public void getSongList() {
        ContentResolver musicResolver = mActivity.getContentResolver();
        Uri musicInternalUri = android.provider.MediaStore.Audio.Media.INTERNAL_CONTENT_URI; // 抓手機內部的音檔
        Cursor musicCursor = musicResolver.query(musicInternalUri, null, null, null, null);
        // Check data is valid or not
        if (musicCursor != null && musicCursor.moveToFirst()) {
            // get data from columns
            int titleColumn = musicCursor.getColumnIndex(PROVIDER_MEDIA_TITLE);
            int idColumn = musicCursor.getColumnIndex(PROVIDER_MEDIA_ID);
            int artistColumn = musicCursor.getColumnIndex(PROVIDER_MEDIA_ARTIST);
            // add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                mSongList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());
        }
    }
}
