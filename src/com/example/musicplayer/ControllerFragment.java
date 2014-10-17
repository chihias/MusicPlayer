
package com.example.musicplayer;

import java.util.ArrayList;

import com.example.musicplayer.MusicService.MusicBinder;
import com.example.musicplayer.MusicService.OnHeadsetPlugOutListener;
import com.example.musicplayer.MusicService.OnMusicStateListener;
import com.example.musicplayer.MusicService.OnNotificationBtnClickedListener;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ControllerFragment extends Fragment implements View.OnClickListener {

    public final static String ARG_POSITION = "position";
    public static final String PROVIDER_MEDIA_DATA = "android.provider.MediaStore.Audio.Media.DATA";
    public static final String PROVIDER_MEDIA_ID = "android.provider.MediaStore.Audio.Media._ID";

    private Activity mActivity;
    private Button mPlayandPauseButton;
    private Button mPrevButton;
    private Button mNextButton;
    private FrameLayout mControllerFrag;
    private ImageView mAlbumImage;
    private TextView mSongTitleTextView;
    private TextView mSongArtistTextView;
    private TextView mSongDurationTextView;
    private SeekBar mSeekBar;
    private Utilities mUtils;

    private Handler mHandler;
    private long mCurrentProcess;
    private long mMaxProcess;
    private final int PRO = 1;

    // private Button mStopButton;
    private MusicService mMusicSrv = null;
    private Intent mPlayIntent;
    private ArrayList<Song> mSongList;

    private int mCurrentSongId;
    private ServiceConnection mMusicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("123", "ControllerFragment: onServiceConnected");
            MusicBinder binder = (MusicBinder) service;
            mMusicSrv = binder.getService();
            //musicBound = true;

            /* Music Service Prepared */
            mMusicSrv.setOnMusicStateListener(new OnMusicStateListener() {

                @Override
                public void onMusicPrepareCompleteListener() {
                    Log.d("123", "MusicSrvPrepared");
                    updateControllerView();
                }

            });

            /* Headset Plug Out */
            mMusicSrv.setOnHeadsetPlugOutListener(new OnHeadsetPlugOutListener() {

                @Override
                public void updateControllerViewAfterPlugOutHeadset() {
                    Log.d("123", "ControllerFragment: Plug out and refresh");
                    updateControllerView();
                }

            });

            /* Notification Btn Clicked */
            mMusicSrv.setOnNotificationBtnClickedListener(new OnNotificationBtnClickedListener() {

                @Override
                public void updateControllerViewAfterPlayAndPauseBtnClicked() {
                    Log.i("123", "ControllerFragment: notification playandpause button clicked");
                    updateControllerView();
                }

                @Override
                public void stopServiceAfterStopBtnClicked() {
                    setMusicSrvEnd();
                }
            });

            mMusicSrv.setList(mSongList);

            /* End Activity and Start Activity Again */
            if (mMusicSrv.isPaused() || mMusicSrv.isPng()) {
                updateControllerView();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // mMusicSrv = null;
            // mActivity.stopService(mPlayIntent);
        }
    };

    private Runnable mUpdateSeekbarTask = new Runnable() {

        @Override
        public void run() {
            //Log.d("123", "mUpdateSeekbarTask");
            mMaxProcess = mMusicSrv.getDur();
            mCurrentProcess = mMusicSrv.getPosn();
            mSongDurationTextView.setText(mUtils.milliSecondsToTimer(mMusicSrv.getDur()));
            int progress = (int) mUtils.getProgressPercentage(mCurrentProcess, mMaxProcess);
            mSeekBar.setProgress(progress);
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.controller_fragment, container, false);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_play_and_pause:

                if (mMusicSrv.isPng()) {
                    Log.i("123", "ControllerFragment: is paused");
                    mMusicSrv.pausePlayer();
                    mPlayandPauseButton.setBackgroundResource(R.drawable.play_btn);
                    break;
                } else {
                    Log.i("123", "ControllerFragment: is playing");
                    mMusicSrv.goPlay();
                    mPlayandPauseButton.setBackgroundResource(R.drawable.pause_btn);
                    break;
                }
            case R.id.button_prevsong:
                mMusicSrv.playPrev();
                mPlayandPauseButton.setBackgroundResource(R.drawable.pause_btn);
                break;
            case R.id.button_nextsong:
                mMusicSrv.playNext();
                mPlayandPauseButton.setBackgroundResource(R.drawable.pause_btn);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        // outState.putInt(ARG_POSITION, mCurrentSongId);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("123", "ControllerFragment: onStart");
        mPlayIntent = new Intent(mActivity, MusicService.class);
        mActivity.bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
        Toast.makeText(mActivity, "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        Log.i("123", "ControllerFragment: onResume");
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mHandler != null){
            mHandler.removeCallbacks(mUpdateSeekbarTask);
        }
        mMusicSrv.checkStopself();
        // mActivity.stopService(mPlayIntent); //why?
        mMusicSrv.setmOnHeadsetPlugOutListenerNull();
        mMusicSrv.setmOnMusicStateListenerNull();
        mMusicSrv.setmOnNotificationBtnClickedListenerNull();
        Toast.makeText(mActivity, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void initialize() {
        mActivity = getActivity();
        mUtils = new Utilities();
        mHandler = new Handler();
        setupViewComponent();
        initialView();
    }

    private void setupViewComponent() {
        mPlayandPauseButton = (Button) getView().findViewById(R.id.button_play_and_pause);
        mPrevButton = (Button) getView().findViewById(R.id.button_prevsong);
        mNextButton = (Button) getView().findViewById(R.id.button_nextsong);
        mAlbumImage = (ImageView) getView().findViewById(R.id.album_image);
        mSeekBar = (SeekBar) getView().findViewById(R.id.seekBar_songProcess);
        mSongTitleTextView = (TextView) getView().findViewById(R.id.textview_songtitle);
        mSongArtistTextView = (TextView) getView().findViewById(R.id.textview_artist);
        mSongDurationTextView = (TextView) getView().findViewById(R.id.textview_songduration);
        mControllerFrag = (FrameLayout) getView().findViewById(R.id.controller_fragment_container);

    }

    private void initialView() {
        mPlayandPauseButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mSeekBar.setProgress(0);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.i("123", "ControllerFragment: onStartTrackingTouch");
                mHandler.removeCallbacks(mUpdateSeekbarTask);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i("123", "ControllerFragment: onStopTrackingTouch");
                mHandler.removeCallbacks(mUpdateSeekbarTask);
                mMaxProcess = mMusicSrv.getDur();
                mMusicSrv.seek(mUtils.progressToTimer((int) seekBar.getProgress(),
                        (int) mMaxProcess));
                mHandler.post(mUpdateSeekbarTask);
            }
        });
    }

    public void updateControllerView() {
        mSongTitleTextView.setText(mMusicSrv.getCurrentSongTitle());
        mSongArtistTextView.setText(mMusicSrv.getCurrentSongArtist());

//        MediaMetadataRetriever mediaMetadataretriever = new MediaMetadataRetriever();
//        mediaMetadataretriever.setDataSource(getSongPath());
//        byte[] embeddedpic = mediaMetadataretriever.getEmbeddedPicture();
//        if( embeddedpic != null){
//            Log.d("123", "embeddedpic exist");
//            //mAlbumImage.setImageBitmap(BitmapFactory.decodeByteArray(embeddedpic, 0, embeddedpic.length));
//        }else{
//            mAlbumImage.setImageResource(R.drawable.no_album_image);
//        }

        mHandler.post(mUpdateSeekbarTask);
        mPlayandPauseButton.setBackgroundResource(R.drawable.pause_btn);
        if (mMusicSrv.isPaused()) {
            mPlayandPauseButton.setBackgroundResource(R.drawable.play_btn);
        }
    }

    public void onPlaySong(View view) {
        mActivity.startService(mPlayIntent);
        // mActivity.bindService(mPlayIntent, mMusicConnection,
        // Context.BIND_AUTO_CREATE);
        /* remove callback before click other songs */
        mHandler.removeCallbacks(mUpdateSeekbarTask);
        mCurrentSongId = Integer.parseInt(view.getTag().toString());
        mMusicSrv.setSong(mCurrentSongId);
        Log.i("123", "ControllerFragment: onPlaySong");
        mMusicSrv.playSong();
        // updateControllerView();

    }

    public void setMySongList(ArrayList<Song> list) {
        mSongList = list;
    }

    public void setMusicSrvShuffle() {
        mMusicSrv.setShuffle();
    }

    public void setMusicSrvEnd() {
        /*
         * if (mMusicSrv != null) { mMusicSrv = null;
         * mActivity.unbindService(mMusicConnection);
         * mActivity.stopService(mPlayIntent); // System.exit(0); }
         */
        mActivity.stopService(mPlayIntent);
        mActivity.finish();
    }

    public String getSongPath(){
        ContentResolver musicResolver = mActivity.getContentResolver();
        Uri musicExternalUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //String[] proj = { PROVIDER_MEDIA_DATA };
        String[] proj = { android.provider.MediaStore.Audio.Media.DATA };
        
        Cursor musicCursor = musicResolver.query(musicExternalUri, proj, android.provider.MediaStore.Audio.Media._ID + "=?", new String[] { String.valueOf(mCurrentSongId) } , null);
        Log.e("123",""+ mCurrentSongId);
        musicCursor.moveToFirst();
        Log.e("123",""+ musicCursor.getCount());
        int pathColumn = musicCursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DATA);
        String src = musicCursor.getString(pathColumn);
        Log.d("123","path= " + src);
        return src;
    }
}
