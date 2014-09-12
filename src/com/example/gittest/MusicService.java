
package com.example.gittest;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service {

    public static final String ACTION_PLAY_MUSIC = "com.example.gittest.ACTION_PLAY_MUSIC";
    public static final String ACTION_PAUSE_MUSIC = "com.example.gittest.ACTION_PAUSE_MUSIC";
    public static final String ACTION_STOP_MUSIC = "com.example.gittest.ACTION_STOP_MUSIC";

    private MediaPlayer mMediaPlayer;

    public class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Uri uri = Uri.parse("/Music/imyours.mp3");
        mMediaPlayer = MediaPlayer.create(this, uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_PLAY_MUSIC.equals(intent.getAction())) {

        } else if (ACTION_PAUSE_MUSIC.equals(intent.getAction())) {
            // intent.setAction(Intent.ACTION_SEND);
        } else if (ACTION_STOP_MUSIC.equals(intent.getAction())) {

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
