
package com.example.musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.musicplayer.MusicService.MusicBinder;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;

public class ListFragment extends Fragment implements MediaPlayerControl {

    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {// mActivity要先initialize
        super.onAttach(activity);
        mActivity = activity;
    }

    private ArrayList<Song> mSongList;
    private ListView mSongView;

    public static final String PROVIDER_MEDIA_TITLE = "android.provider.MediaStore.Audio.Media.TITLE";
    public static final String PROVIDER_MEDIA_ID = "android.provider.MediaStore.Audio.Media._ID";
    public static final String PROVIDER_MEDIA_ARTIST = "android.provider.MediaStore.Audio.Media.ARTIST";

    private MusicService mMusicSrv;
    private Intent mPlayIntent;
    private boolean mMusicBound = false;
    private MusicController mController;
    private ServiceConnection mMusicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            mMusicSrv = binder.getService();
            mMusicSrv.setList(mSongList);
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mMusicBound = false;
        }
    };

    private boolean mPaused = false, mPlaybackPaused = false;

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
        SongAdapter songAdt = new SongAdapter(mActivity, mSongList);
        mSongView.setAdapter(songAdt);
        mSongView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                songPicked(arg1);
            }
        });
        setController();
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
        Uri musicInternalUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; 
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                mMusicSrv.setShuffle();
                break;
            case R.id.action_end:
                mActivity.stopService(mPlayIntent);
                System.exit(0);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(mActivity, MusicService.class);
            mActivity.bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
            mActivity.startService(mPlayIntent);
        }
    }

    @Override
    public void onDestroy() {
        mActivity.stopService(mPlayIntent);
        mMusicSrv = null;
        super.onDestroy();
    }

    public void songPicked(View view) {
        mMusicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        mMusicSrv.playSong();
        if(mPlaybackPaused){
            setController();
            mPlaybackPaused = false;
        }
        mController.show(0);
    }

    private void setController() {
        mController = new MusicController(mActivity);
        mController.setPrevNextListeners(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        mController.setMediaPlayer(this);
        mController.setAnchorView(mActivity.findViewById(R.id.song_list));
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mMusicSrv != null && mMusicBound && mMusicSrv.isPng())
            return mMusicSrv.getPosn();
        else
            return 0;
    }

    @Override
    public int getDuration() {
        if (mMusicSrv != null && mMusicBound && mMusicSrv.isPng())
            return mMusicSrv.getDur();
        else
            return 0;
    }

    @Override
    public boolean isPlaying() {
        if (mMusicSrv != null && mMusicBound)
            return mMusicSrv.isPng();
        return false;
    }

    @Override
    public void pause() {
        mPlaybackPaused = true;
        mMusicSrv.pausePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPaused) {
            setController();
            mPaused = false;
        }
    }

    @Override
    public void onStop() {
        mController.hide();
        super.onStop();
    }

    @Override
    public void seekTo(int posn) {
        mMusicSrv.seek(posn);
    }

    @Override
    public void start() {
        mMusicSrv.goPlay();
    }

    private void playNext() {
        mMusicSrv.playNext();
        if(mPlaybackPaused){
            setController();
            mPlaybackPaused = false;
        }
        mController.show(0);
    }

    private void playPrev() {
        mMusicSrv.playPrev();
        if(mPlaybackPaused){
            setController();
            mPlaybackPaused = false;
        }
        mController.show(0);
    }

}
