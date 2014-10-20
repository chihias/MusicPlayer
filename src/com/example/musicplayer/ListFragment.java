
package com.example.musicplayer;

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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class ListFragment extends Fragment {

    public static final String PROVIDER_MEDIA_TITLE = "android.provider.MediaStore.Audio.Media.TITLE";
    public static final String PROVIDER_MEDIA_ID = "android.provider.MediaStore.Audio.Media._ID";
    public static final String PROVIDER_MEDIA_ARTIST = "android.provider.MediaStore.Audio.Media.ARTIST";
    public ArrayList<Song> mSongList;

    public interface OnPlaySongListener {
        public void onSongSelected(View view);

        public void getSongList(ArrayList<Song> list);
    }

    private Activity mActivity;
    private ListView mSongView;
    private OnPlaySongListener mOnPlaySongListener;

    @Override
    public void onAttach(Activity activity) {// mActivity要先initialize
        super.onAttach(activity);
        mActivity = activity;
        try {
            mOnPlaySongListener = (OnPlaySongListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnPlaySongListener");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) { // 在activity底下放在create的內容
                                                               // 在fragment要放在activitycreate
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
        mOnPlaySongListener.getSongList(mSongList);
        SongAdapter songAdt = new SongAdapter(mActivity, mSongList);
        mSongView.setAdapter(songAdt);
        mSongView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mOnPlaySongListener.onSongSelected(arg1);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(mActivity, "onCreate", Toast.LENGTH_LONG);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        Toast.makeText(mActivity, "onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(mActivity, "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        Toast.makeText(mActivity, "onStop", Toast.LENGTH_SHORT).show();
    }

    // a helper method
    public void getSongList() {
        ContentResolver musicResolver = mActivity.getContentResolver();
        Uri musicExternalUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicExternalUri, null, null, null, null);
        // Check data is valid or not
        try {
            if (musicCursor != null && musicCursor.moveToFirst()) {
                // get data from columns
                int titleColumn = musicCursor.getColumnIndex(PROVIDER_MEDIA_TITLE);
                int idColumn = musicCursor.getColumnIndex(PROVIDER_MEDIA_ID);
                int artistColumn = musicCursor.getColumnIndex(PROVIDER_MEDIA_ARTIST);
                // int dataColumn =
                // musicCursor.getColumnIndex(PROVIDER_MEDIA_DATA);

                // add songs to list
                do {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    // String thisPath = musicCursor.getString(dataColumn);
                    // Log.i("123","Path" + thisPath);
                    mSongList.add(new Song(thisId, thisTitle, thisArtist));
                } while (musicCursor.moveToNext());
            }
        } finally {
            musicCursor.close();
        }
    }

    private void setupViewComponent() {
        mSongView = (ListView) getView().findViewById(R.id.song_list);
    }

    private void instanList() {
        mSongList = new ArrayList<Song>();
    }

}
