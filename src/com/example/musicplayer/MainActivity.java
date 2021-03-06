package com.example.musicplayer;

import java.util.ArrayList;

import com.example.musicplayer.ControllerFragment.OnServiceStateListener;
import com.example.musicplayer.ListFragment.OnPlaySongListener;
import com.example.musicplayer.MusicService.MusicBinder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements OnPlaySongListener {

    private ControllerFragment mControllerFragment;
    private ListFragment mListFragment;
    private View mControlFragContainer;
    private static final String TAG = "MUSIC_PLAYER_MAIN_ACTIVITY";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            return;
        }

        mListFragment = new ListFragment();
        mControllerFragment = new ControllerFragment();
        mControlFragContainer = findViewById(R.id.controller_fragment_container);

        if (findViewById(R.id.list_fragment_container) != null) {
            addListFragment();
        }
        if (findViewById(R.id.controller_fragment_container) != null) {
            addControllerFragment();
        }
//        /* There is a bug on certain devices*/
//        if (isServiceRunning(MusicService.class)) {
//            setControlFragContainerVisiblity();
//        }

        /* Set controller Fragment visible or not */
        if(MusicService.class != null && mControllerFragment != null){

            /* Make sure service is connected before checking whether music is play-or-pause or not */
            mControllerFragment.setOnServiceConnectedListener(new OnServiceStateListener() {

                @Override
                public void onServiceConnectedListener() {
                    // TODO Auto-generated method stub
                    if(mControllerFragment.checkServiceRunning()){
                        setControlFragContainerVisiblity();
                    }
                }

            });
        }

        Log.i(TAG, " onCreate");

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
            case R.id.action_shuffle:
                mControllerFragment.setMusicSrvShuffle();
                break;*/
            case R.id.action_end:
                mControllerFragment.setMusicSrvEnd();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSongSelected(View view) {
        // Toast.makeText(this,
        // Boolean.toString(this.getControlFragContainerVisibility()),
        // Toast.LENGTH_LONG).show();
        if (!this.getControlFragContainerVisibility()) {
            setControlFragContainerVisiblity();
        }
        mControllerFragment.onPlaySong(view);
    }

    @Override
    public void getSongList(ArrayList<Song> list) {
        mControllerFragment.setMySongList(list);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Toast.makeText(this, "From receiver", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // mControllerFragment.stopMusicService();
        // stopService(new Intent(this,MusicService.class));

        /* Make Sure Listener is closed */
        mControllerFragment.setmOnServiceStateListenerNull();
        Log.i(TAG, "onDestroy");
    }

    private void addControllerFragment() {
        FragmentManager fragMng = getFragmentManager();
        FragmentTransaction fragTran = fragMng.beginTransaction();
        fragTran.add(R.id.controller_fragment_container, mControllerFragment, "controller");
        fragTran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragTran.commit();
    }

    private void addListFragment() {
        FragmentManager fragMng = getFragmentManager();
        FragmentTransaction fragTran = fragMng.beginTransaction();
        fragTran.add(R.id.list_fragment_container, mListFragment, "list");
        fragTran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragTran.commit();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean getControlFragContainerVisibility() {
        if (mControlFragContainer.getVisibility() == View.VISIBLE)
            return true;
        else
            return false;
    }

    private void setControlFragContainerVisiblity() {
        mControlFragContainer.setVisibility(View.VISIBLE);
    }

}
