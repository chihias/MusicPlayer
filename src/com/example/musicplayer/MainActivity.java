
package com.example.musicplayer;

import java.util.ArrayList;

import com.example.musicplayer.ListFragment.OnPlaySongListener;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity implements OnPlaySongListener {

    private ControllerFragment mControllerFragment;
    private ListFragment mListFragment;
    private View mControlFragContainer;
    private HeadsetPlugReceiver mHeadsetPlugReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListFragment = new ListFragment();
        mControllerFragment = new ControllerFragment();
        mControlFragContainer = findViewById(R.id.controller_fragment_container);

        if (findViewById(R.id.list_fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            addListFragment();
        }
        if (findViewById(R.id.controller_fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            addControllerFragment();
        }
       // registerHeadsetPlugReceiver();
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
            case R.id.action_shuffle:
                mControllerFragment.setMusicSrvShuffle();
                break;
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
        if (this.getControlFragContainerVisibility()) {

        } else {
            setControlFragContainerVisiblity();
        }
        mControllerFragment.onPlaySong(view);
    }

    @Override
    public void getSongList(ArrayList<Song> list) {
        mControllerFragment.setMySongList(list);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver();
        //mControllerFragment.stopMusicService();
        //stopService(new Intent(this,MusicService.class));
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
/*
    private void registerHeadsetPlugReceiver(){
        mHeadsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver();
    }

    private void registerReceiver(){
        
    }

    private void unregisterReceiver(){
        this.unregisterReceiver(mHeadsetPlugReceiver);
    }
*/
    public boolean getControlFragContainerVisibility() {
        if (mControlFragContainer.getVisibility() == View.VISIBLE)
            return true;
        else
            return false;
    }

    public void setControlFragContainerVisiblity() {
        mControlFragContainer.setVisibility(View.VISIBLE);
    }
    

}
