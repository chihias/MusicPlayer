
package com.example.gittest;

import android.R.string;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class playerFragment extends Fragment implements View.OnClickListener {

    private Activity mActivity;

    private Button mPlayButton;
    private Button mPauseButton;
    private Button mStopButton;

    private final String LOG_TAG = "music player demo";

    private MusicService mMusicService = null;
    private ServiceConnection mServiceConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "onServiceConnected()" + name.getClassName());
            mMusicService = ((MusicService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "onServiceDiconnected()" + name.getClassName());

        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();
    }

    private void initialize() {
        mActivity = getActivity();
        setupViewComponent();
        initButton();

    }

    private void setupViewComponent() {
        mPlayButton = (Button) getView().findViewById(R.id.button_play);
        mPauseButton = (Button) getView().findViewById(R.id.button_pause);
        mStopButton = (Button) getView().findViewById(R.id.button_stop);
    }

    private void initButton() {
        mPlayButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_fragment, container, false);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_play:
                Intent playIntent = new Intent(mActivity, MusicService.class);
                playIntent.setAction(MusicService.ACTION_PLAY_MUSIC);
                mActivity.startService(playIntent);
                mActivity.bindService(playIntent, mServiceConn, mActivity.BIND_AUTO_CREATE);
                break;
            case R.id.button_pause:
                Intent pauseIntent = new Intent(mActivity, MusicService.class);
                pauseIntent.setAction(MusicService.ACTION_PLAY_MUSIC);
                mActivity.startService(pauseIntent);
                break;
            case R.id.button_stop:
                Intent stopIntent = new Intent(mActivity, MusicService.class);
                mActivity.stopService(stopIntent);
                break;

        }
    }
}
