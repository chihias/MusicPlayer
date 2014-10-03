
package com.example.musicplayer;

import java.util.ArrayList;

import com.example.musicplayer.MusicService.MusicBinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

public class HeadsetPlugReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("state")) {
            // Intent musicServicePauseIntent = new Intent(context,
            // MusicService.class);
            if (intent.getIntExtra("state", 0) == 0) {
                // pause music service
                // musicServicePauseIntent.putExtra("pauseIntent", 1);
                // mMusicService.startService(musicServicePauseIntent);
                // MusicService.pausePlayer();
                IBinder service = peekService(context, new Intent(context, MusicService.class));
                MusicBinder binder = (MusicBinder) service;
                MusicService musicService = binder.getService();
                if (musicService.isPng()) {
                    musicService.pausePlayer();
                }

                Intent musicServicePauseIntent = new Intent(context, MusicService.class);
                musicServicePauseIntent.setAction("refresh_ui");
                context.startService(musicServicePauseIntent);

                Log.e("123", "headset not connected");
            } else if (intent.getIntExtra("state", 0) == 1) {
                Log.e("123", "headset connected");
            }
        }
    }

}
