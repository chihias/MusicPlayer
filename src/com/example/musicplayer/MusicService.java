
package com.example.musicplayer;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public interface OnMusicStateListener {
        public void onMusicPrepareCompleteListener();
    }

    public interface OnHeadsetPlugOutListener {
        public void updateControllerViewAfterPlugOutHeadset();
    }

    public interface OnNotificationBtnClickedListener {
        public void updateControllerViewAfterPlayAndPauseBtnClicked();

        public void stopServiceAfterStopBtnClicked();
    }

    public static final String ACTION_PLAY_AND_PAUSE_MUSIC = "com.example.musicplayer.ACTION_PLAY_AND_PAUSE_MUSIC";
    public static final String ACTION_STOP_MUSIC = "com.example.musicplayer.ACTION_STOP_MUSIC";

    private MediaPlayer mMediaPlayer;
    private ArrayList<Song> mSongs;
    private int mSongPosition;
    private final IBinder mMusicBind = new MusicBinder();
    private String mSongTitle = "";
    private String mSongArtist = "";
    private static final int NOTIFY_ID = 1;
    private boolean mShuffle = false;
    private boolean mServicePaused = false;
    private Random rand;

    private RemoteViews mContentView;
    private Notification.Builder mBuilder;

    private OnMusicStateListener mOnMusicStateListener;
    private OnHeadsetPlugOutListener mOnHeadsetPlugOutListener;
    private OnNotificationBtnClickedListener mOnNotificationBtnClickedListener;

    private HeadsetPlugReceiver mHeadsetPlugReceiver;
    private NotificationBtnClickedReceiver mBtnClickedReceiver;

    public class NotificationBtnClickedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
//
//            Intent notIntent = new Intent(context, MainActivity.class);
//            notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent pendInt = PendingIntent.getActivity(context, 0, notIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//
//            Intent playAndPauseBtnClicked = new Intent();
//            playAndPauseBtnClicked.setAction(ACTION_PLAY_AND_PAUSE_MUSIC);
//            PendingIntent pendInt_playAndPause = PendingIntent.getBroadcast(context, 0,
//                    playAndPauseBtnClicked, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            Intent stopBtnClicked = new Intent();
//            stopBtnClicked.setAction(ACTION_STOP_MUSIC);
//            PendingIntent pendInt_stop = PendingIntent.getBroadcast(context, 0, stopBtnClicked,
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//
//            Notification.Builder builder = new Notification.Builder(context);
//            builder.setSmallIcon(R.drawable.headset_blue).setContentIntent(pendInt);


//            RemoteViews remoteviews = new RemoteViews(getPackageName(), R.layout.music_player_notification);
//            remoteviews.setImageViewResource(R.id.notification_icon, R.drawable.headset_white);
//            remoteviews.setTextViewText(R.id.notification_artist_title, mSongArtist);
//            remoteviews.setTextViewText(R.id.notification_song_title, mSongTitle);
//            remoteviews.setOnClickPendingIntent(R.id.notification_playandpause_button,
//                    pendInt_playAndPause);
//            remoteviews.setOnClickPendingIntent(R.id.notification_stop_button, pendInt_stop);

            if (ACTION_PLAY_AND_PAUSE_MUSIC.equals(intent.getAction())) {
                if (isPng() == true) {
                    Log.e("123", "nof_isPng");
                    Drawable d = context.getResources().getDrawable(R.drawable.play_btn);
                    Log.e("123", "Drawable d " + (d == null ? "not" : "") + " exists");
                    mContentView.setImageViewResource(R.id.notification_playandpause_button,
                            R.drawable.play_btn);
                    Log.e("123", "pauseplayer");
                    pausePlayer();
                } else if (isPaused() == true) {
                    Log.e("123", "nof_isPaused");
                    mContentView.setImageViewResource(R.id.notification_playandpause_button,
                            R.drawable.pause_btn);
                    Log.e("123", "goplay");
                    goPlay();
                }
                if (mOnNotificationBtnClickedListener != null) {
                    mOnNotificationBtnClickedListener
                            .updateControllerViewAfterPlayAndPauseBtnClicked();
                }
                mBuilder.setContent(mContentView);
                notificationManager.notify(NOTIFY_ID, mBuilder.build());
            } else if (ACTION_STOP_MUSIC.equals(intent.getAction())) {
                if (mOnNotificationBtnClickedListener != null) {
                    mOnNotificationBtnClickedListener.stopServiceAfterStopBtnClicked();
                }
                Log.e("123", "stop");
                stopForeground(true);
                stopSelf();
            }

            // AppWidgetManager appWidgetManager =
            // AppWidgetManager.getInstance(context);
            // ComponentName componentName = new ComponentName(context,
            // MusicService.class);
            // appWidgetManager.updateAppWidget(componentName, remoteviews);
        }
    }

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
        registerHeadsetPlugReceiver();
        registerBtnClickedReceiver();
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
        Log.e("123", "onDestroy");
        mMediaPlayer.release();
        super.onDestroy();
        unregisterReceiver();
    }

    public void stop() {
        Log.e("123", "stop");
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    public void checkStopself() {
        Log.e("123", "checkStopself");
        if (!mMediaPlayer.isPlaying() && !this.isPaused()) {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("123", "onStartCommand");
        if ("refresh_ui".equals(intent.getAction())) {
            if (mOnHeadsetPlugOutListener != null) {
                mOnHeadsetPlugOutListener.updateControllerViewAfterPlugOutHeadset();
            }
            Log.e("123", "Refresh UI");
        }
        // if (intent != null) {
        // int intPauseIntent = intent.getIntExtra("pauseIntent", 0);
        // if (intPauseIntent == 1) {
        // this.pausePlayer();
        // }
        // }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("123", "onUnbind");
        // mMediaPlayer.stop();
        // mMediaPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("123", "onCompletion");
        if (mMediaPlayer.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("123", "onError");
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e("123", "onPrepared");
        mp.start();
        if (mOnMusicStateListener != null) {
            mOnMusicStateListener.onMusicPrepareCompleteListener();
        }

        // Intent notIntent = new Intent(this, MainActivity.class);
        // notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
        // PendingIntent.FLAG_UPDATE_CURRENT);
        // Notification.Builder builder = new Notification.Builder(this);
        // builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play).setTicker(mSongTitle)
        // .setOngoing(true).setContentTitle("Playing").setContentText(mSongTitle);
        // Notification nof = builder.build();
        // startForeground(NOTIFY_ID, nof);
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playAndPauseBtnClicked = new Intent();
        playAndPauseBtnClicked.setAction(ACTION_PLAY_AND_PAUSE_MUSIC);
        PendingIntent pendInt_playAndPause = PendingIntent.getBroadcast(this, 0,
                playAndPauseBtnClicked, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopBtnClicked = new Intent();
        stopBtnClicked.setAction(ACTION_STOP_MUSIC);
        PendingIntent pendInt_stop = PendingIntent.getBroadcast(this, 0, stopBtnClicked,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mContentView = new RemoteViews(getPackageName(), R.layout.music_player_notification);
        mBuilder = new Notification.Builder(this);

        mBuilder.setSmallIcon(R.drawable.headset_blue).setContent(mContentView)
                .setContentIntent(pendInt);

        mContentView.setImageViewResource(R.id.notification_icon, R.drawable.headset_white);
        mContentView.setTextViewText(R.id.notification_artist_title, mSongArtist);
        mContentView.setTextViewText(R.id.notification_song_title, mSongTitle);
        mContentView.setOnClickPendingIntent(R.id.notification_playandpause_button,
                pendInt_playAndPause);
        mContentView.setOnClickPendingIntent(R.id.notification_stop_button, pendInt_stop);
        mContentView.setImageViewResource(R.id.notification_playandpause_button,
                R.drawable.pause_btn);
        Notification nof = mBuilder.build();
        startForeground(NOTIFY_ID, nof);

    }

    public void setSong(int songIndex) {
        Log.e("123", "setSong");
        mSongPosition = songIndex;
    }

    public void playSong() {
        mMediaPlayer.reset();
        Song playSong = mSongs.get(mSongPosition);
        mSongTitle = playSong.getTitle();
        mSongArtist = playSong.getArtist();
        Log.e("123", "playSong,  position=" + mSongPosition + ", title = " + mSongTitle
                + ", artist = " + mSongArtist);
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mServicePaused = false;
        mMediaPlayer.prepareAsync();
    }

    public String getCurrentSongTitle() {
        Log.e("123", "getCurrentSongTitle=" + mSongTitle + ", mSongPosition=" + mSongPosition);
        return mSongTitle;
    }

    public String getCurrentSongArtist() {
        return mSongArtist;
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

    public boolean isPaused() {
        return this.mServicePaused;
    }

    public void pausePlayer() {
        mMediaPlayer.pause();
        mServicePaused = true;
    }

    public void seek(int posn) {
        mMediaPlayer.seekTo(posn);
    }

    public void goPlay() {
        mMediaPlayer.start();
        mServicePaused = false;
    }

    public void playPrev() {
        Log.e("123", "playPrev");
        mSongPosition--;
        if (mSongPosition < 0)
            mSongPosition = mSongs.size() - 1;
        playSong();
    }

    public void playNext() {
        Log.e("123", "playNext");
        if (mShuffle) {
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
        if (mShuffle)
            mShuffle = false;
        else
            mShuffle = true;
    }

    public void setOnMusicStateListener(OnMusicStateListener listener) {
        mOnMusicStateListener = listener;
    }

    public void setOnHeadsetPlugOutListener(OnHeadsetPlugOutListener listener) {
        mOnHeadsetPlugOutListener = listener;
    }

    public void setOnNotificationBtnClickedListener(OnNotificationBtnClickedListener listener) {
        mOnNotificationBtnClickedListener = listener;
    }

    public void setmOnHeadsetPlugOutListenerNull() {
        mOnHeadsetPlugOutListener = null;
    }

    public void setmOnMusicStateListenerNull() {
        mOnMusicStateListener = null;
    }

    public void setmOnNotificationBtnClickedListenerNull() {
        mOnNotificationBtnClickedListener = null;
    }

    private void registerHeadsetPlugReceiver() {
        mHeadsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.HEADSET_PLUG");
        this.registerReceiver(mHeadsetPlugReceiver, filter);
    }

    private void registerBtnClickedReceiver() {
        mBtnClickedReceiver = new NotificationBtnClickedReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_AND_PAUSE_MUSIC);
        filter.addAction(ACTION_STOP_MUSIC);
        this.registerReceiver(mBtnClickedReceiver, filter);
    }

    private void unregisterReceiver() {
        this.unregisterReceiver(mHeadsetPlugReceiver);
        this.unregisterReceiver(mBtnClickedReceiver);
    }
}
