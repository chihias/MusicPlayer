
package com.example.musicplayer;

import java.util.ArrayList;

import com.example.musicplayer.MusicService.MusicBinder;
import com.example.musicplayer.MusicService.OnMusicStateListener;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class ControllerFragment extends Fragment implements View.OnClickListener {

    public final static String ARG_POSITION = "position";

    private Activity mActivity;
    private Button mPlayandPauseButton;
    private Button mPrevButton;
    private Button mNextButton;
    private FrameLayout mControllerFrag;
    private TextView mSongTitleTextView;
    private TextView mSongArtistTextView;
    private TextView mSongDurationTextView;
    private SeekBar mSeekBar;

    private Handler mHandler;
    private int mCurrentProcess;
    private int mMaxProcess;
    private final int PRO = 1;

    // private Button mStopButton;
    private MusicService mMusicSrv = null;
    private boolean mMusicBound = false;
    private boolean mPlaybackPaused = false;
    private Intent mPlayIntent;
    private ArrayList<Song> mSongList;

    private int mCurrentSongId;
    // private int mCurrentPosition = -1;
    private final String LOG_TAG = "music player demo";
    private ServiceConnection mMusicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("123", "onServiceConnected");
            MusicBinder binder = (MusicBinder) service;
            mMusicSrv = binder.getService();
            mMusicSrv.setOnMusicStateListener(new OnMusicStateListener() {

                @Override
                public void onMusicPrepareCompleteListener() {
                    updateControllerView();
                }

            });
            mMusicSrv.setList(mSongList);
            mMusicBound = true;
            if (mMusicSrv.isPaused() || mMusicSrv.isPng()) {
                updateControllerView();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mMusicBound = false;
            // mMusicSrv = null;
            // mActivity.stopService(mPlayIntent);
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
                    Log.e("123", "is paused");
                    mMusicSrv.pausePlayer();
                    mPlayandPauseButton.setBackgroundResource(R.drawable.play_btn);
                    break;
                } else {
                    Log.e("123", "is playing");
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
        Log.e("123", "onStart");
        mPlayIntent = new Intent(mActivity, MusicService.class);
        mActivity.bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
        Toast.makeText(mActivity, "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        Log.e("123", "onResume");
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMusicSrv.checkStopself();
        // mActivity.stopService(mPlayIntent); //why?
        Toast.makeText(mActivity, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void initialize() {
        mActivity = getActivity();
        setupViewComponent();
        initialView();
    }

    private void setupViewComponent() {
        mPlayandPauseButton = (Button) getView().findViewById(R.id.button_play_and_pause);
        mPrevButton = (Button) getView().findViewById(R.id.button_prevsong);
        mNextButton = (Button) getView().findViewById(R.id.button_nextsong);
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

    }

    public void updateControllerView() {
        Log.e("123", "updateControllerView");
        mSongTitleTextView.setText(mMusicSrv.getCurrentSongTitle());
        mSongArtistTextView.setText(mMusicSrv.getCurrentSongArtist());
        // mSongDurationTextView.setText(mMusicSrv.getDur());
        // Log.e("123", "Song duration = " + mMusicSrv.getDur());
        // Log.e("123", "dur= " + String.valueOf(mMusicSrv.getDur()));

//        mCurrentProcess = mMusicSrv.getPosn();
//        mMaxProcess = mMusicSrv.getDur();
//        mHandler = new Handler() {
//
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//
//                switch (msg.what) {
//                    case PRO:
//                        if (mCurrentProcess < mMaxProcess) {
//                            mCurrentProcess += 1;
//                            mSeekBar.incrementProgressBy(1);
//                            mHandler.sendEmptyMessageDelayed(PRO, 60);
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            }
//
//        };
//        mCurrentProcess = mCurrentProcess > 0 ? mCurrentProcess : 0;
//        mSeekBar.setMax(mMaxProcess);
//        mSeekBar.setProgress(mCurrentProcess);
//        mHandler.sendEmptyMessage(PRO);

        mPlayandPauseButton.setBackgroundResource(R.drawable.pause_btn);
        if (mMusicSrv.isPaused()) {
            mPlayandPauseButton.setBackgroundResource(R.drawable.play_btn);
        }

    }

    public void onPlaySong(View view) {
        mActivity.startService(mPlayIntent);
        // mActivity.bindService(mPlayIntent, mMusicConnection,
        // Context.BIND_AUTO_CREATE);
        mCurrentSongId = Integer.parseInt(view.getTag().toString());
        mMusicSrv.setSong(mCurrentSongId);
        Log.e("123", "onPlaySong");
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

}
