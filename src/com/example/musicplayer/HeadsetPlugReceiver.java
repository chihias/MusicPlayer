
package com.example.musicplayer;

import com.example.musicplayer.MusicService.MusicBinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class HeadsetPlugReceiver extends BroadcastReceiver {

    private static final String TAG = "MUSIC_PLAYER_HEADSET_PLUG_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("state")) {
            // Intent musicServicePauseIntent = new Intent(context,
            // MusicService.class);
            if (0 == intent.getIntExtra("state", 0)) {
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

                Log.i(TAG, "headset not connected");
            } else if (1 == intent.getIntExtra("state", 0)) {
                Log.i(TAG, "headset connected");
            }
        }
    }

}
