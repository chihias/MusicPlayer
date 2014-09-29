package com.example.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HeadsetPlugReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra("state")){
            if(intent.getIntExtra("state", 0) == 0){
                Log.e("123", "headset not connected" );
            }else if(intent.getIntExtra("state", 0) == 1){
                Log.e("123", "headset connected" );
            }
        }

    }

}
