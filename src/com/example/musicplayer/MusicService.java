
package com.example.musicplayer;

import java.util.ArrayList;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY_MUSIC = "com.example.musicplayer.ACTION_PLAY_MUSIC";
    public static final String ACTION_PAUSE_MUSIC = "com.example.musicplayer.ACTION_PAUSE_MUSIC";
    public static final String ACTION_STOP_MUSIC = "com.example.musicplayer.ACTION_STOP_MUSIC";

    private MediaPlayer mMediaPlayer;
    private ArrayList<Song> mSongs;
    private int mSongPosition;
    private final IBinder mMusicBind = new MusicBinder();
    private String mSongTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;

    @Override
    public IBinder onBind(Intent arg0) {
        return mMusicBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSongPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initMusicPlayer();
        rand = new Random();
    }

    public void initMusicPlayer() {
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {
        mSongs = theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*
         * if (ACTION_PLAY_MUSIC.equals(intent.getAction())) { } else if
         * (ACTION_PAUSE_MUSIC.equals(intent.getAction())) { //
         * intent.setAction(Intent.ACTION_SEND); } else if
         * (ACTION_STOP_MUSIC.equals(intent.getAction())) { }
         */
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mMediaPlayer.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play).setTicker(mSongTitle)
                .setOngoing(true).setContentTitle("Playing").setContentText(mSongTitle);
        Notification nof = builder.build();
        startForeground(NOTIFY_ID, nof);
    }

    public void setSong(int songIndex) {
        mSongPosition = songIndex;
    }

    public void playSong() {
        mMediaPlayer.reset();
        Song playSong = mSongs.get(mSongPosition);
        mSongTitle = playSong.getTitle();
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mMediaPlayer.prepareAsync();
    }

    public int getPosn() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDur() {
        return mMediaPlayer.getDuration();
    }

    public boolean isPng() {
        return mMediaPlayer.isPlaying();
    }

    public void pausePlayer() {
        mMediaPlayer.pause();
    }

    public void seek(int posn) {
        mMediaPlayer.seekTo(posn);
    }

    public void goPlay() {
        mMediaPlayer.start();
    }

    public void playPrev() {
        mSongPosition--;
        if (mSongPosition < 0)
            mSongPosition = mSongs.size() - 1;
        playSong();
    }

    public void playNext() {
        if (shuffle) {
            int newSong = mSongPosition;
            while (newSong == mSongPosition) {
                newSong = rand.nextInt(mSongs.size());
            }
            mSongPosition = newSong;
        } else {
            mSongPosition++;
            if (mSongPosition >= mSongs.size())
                mSongPosition = 0;
        }
        playSong();
    }

    public void setShuffle() {
        if (shuffle)
            shuffle = false;
        else
            shuffle = true;
    }

}
