
package com.example.musicplayer;

import java.util.ArrayList;

import com.example.musicplayer.MusicService.MusicBinder;
import com.example.musicplayer.MusicService.OnHeadsetPlugOutListener;
import com.example.musicplayer.MusicService.OnMusicStateListener;
import com.example.musicplayer.MusicService.OnNotificationBtnClickedListener;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ControllerFragment extends Fragment implements View.OnClickListener {

    public final static String ARG_POSITION = "position";
    public static final Uri MUSIC_URI = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private Activity mActivity;
    private Button mPlayandPauseButton;
    private Button mPrevButton;
    private Button mNextButton;
    private FrameLayout mControllerFrag;
    private ImageView mAlbumImage;
    private TextView mSongTitleTextView;
    private TextView mSongArtistTextView;
    private TextView mSongDurationTextView;
    private SeekBar mSeekBar;
    private Utilities mUtils;


    private Handler mHandler;
    private long mCurrentProcess;
    private long mMaxProcess;
    private final int PRO = 1;

    // private Button mStopButton;
    private MusicService mMusicSrv = null;
    private Intent mPlayIntent;
    private ArrayList<Song> mSongList;

    private boolean mMusicSrvIsRunning = false;
    private static final String TAG = "MUSIC_PLAYER_CONTROLLER_FRAG";

    private int mCurrentSongId;
    private ServiceConnection mMusicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            MusicBinder binder = (MusicBinder) service;
            mMusicSrv = binder.getService();

            /* Music Service Prepared */
            mMusicSrv.setOnMusicStateListener(new OnMusicStateListener() {

                @Override
                public void onMusicPrepareCompleteListener() {
                    //Log.d(TAG, "MusicSrvPrepared");
                    updateControllerView();
                }

            });
            /* Headset Plug Out */
            mMusicSrv.setOnHeadsetPlugOutListener(new OnHeadsetPlugOutListener() {

                @Override
                public void updateControllerViewAfterPlugOutHeadset() {
                    //Log.d(TAG, "Plug out and refresh");
                    updateControllerView();
                }

            });
            /* Notification Btn Clicked */
            mMusicSrv.setOnNotificationBtnClickedListener(new OnNotificationBtnClickedListener() {

                @Override
                public void updateControllerViewAfterPlayAndPauseBtnClicked() {
                    //Log.d(TAG, "notification playandpause button clicked");
                    updateControllerView();
                }

                @Override
                public void stopServiceAfterStopBtnClicked() {
                    setMusicSrvEnd();
                }
            });

            mMusicSrv.setList(mSongList);

            /* End Activity and Start Activity Again */
            if (mMusicSrv.isPaused() || mMusicSrv.isPng()) {
                updateControllerView();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mMusicSrv = null;
            // mActivity.stopService(mPlayIntent);
        }
    };

    private Runnable mUpdateSeekbarTask = new Runnable() {

        @Override
        public void run() {
            mMaxProcess = mMusicSrv.getDur();
            mCurrentProcess = mMusicSrv.getPosn();
            mSongDurationTextView.setText(mUtils.milliSecondsToTimer(mMusicSrv.getDur()));
            int progress = (int) mUtils.getProgressPercentage(mCurrentProcess, mMaxProcess);
            mSeekBar.setProgress(progress);
            mHandler.postDelayed(this, 1000);
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
                    mMusicSrv.pausePlayer();
                    mPlayandPauseButton.setBackgroundResource(R.drawable.play_btn);
                    break;
                } else {
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
        Log.i(TAG, "onStart");
        mPlayIntent = new Intent(mActivity, MusicService.class);
        mActivity.bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
        //Toast.makeText(mActivity, "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mHandler != null){
            mHandler.removeCallbacks(mUpdateSeekbarTask);
        }
        mMusicSrv.checkStopself();
        // mActivity.stopService(mPlayIntent); //why?
        mMusicSrv.setmOnHeadsetPlugOutListenerNull();
        mMusicSrv.setmOnMusicStateListenerNull();
        mMusicSrv.setmOnNotificationBtnClickedListenerNull();
        
        if (mMusicSrv!=null)         mActivity.unbindService(mMusicConnection);
        
        Log.d(TAG,"onDestroy");
        //Toast.makeText(mActivity, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void initialize() {
        mActivity = getActivity();
        mUtils = new Utilities();
        mHandler = new Handler();
        setupViewComponent();
        initialView();
    }

    private void setupViewComponent() {
        mPlayandPauseButton = (Button) getView().findViewById(R.id.button_play_and_pause);
        mPrevButton = (Button) getView().findViewById(R.id.button_prevsong);
        mNextButton = (Button) getView().findViewById(R.id.button_nextsong);
        mAlbumImage = (ImageView) getView().findViewById(R.id.album_image);
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
        mSeekBar.setProgress(0);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.d(TAG, "onStartTrackingTouch");
                mHandler.removeCallbacks(mUpdateSeekbarTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Log.d(TAG, "onStopTrackingTouch");
                mHandler.removeCallbacks(mUpdateSeekbarTask);
                mMaxProcess = mMusicSrv.getDur();
                mMusicSrv.seek(mUtils.progressToTimer((int) seekBar.getProgress(),
                        (int) mMaxProcess));
                mHandler.post(mUpdateSeekbarTask);
            }
        });

    }

    public void updateControllerView() {
        mSongTitleTextView.setText(mMusicSrv.getCurrentSongTitle());
        mSongArtistTextView.setText(mMusicSrv.getCurrentSongArtist());

        //get embedded picture of each song and set album image
        setAlbumImage();

        mHandler.post(mUpdateSeekbarTask);

        mPlayandPauseButton.setBackgroundResource(R.drawable.pause_btn);
        if (mMusicSrv.isPaused()) {
            mPlayandPauseButton.setBackgroundResource(R.drawable.play_btn);
        }

    }

    public void onPlaySong(View view) {
        mActivity.startService(mPlayIntent);
        // mActivity.bindService(mPlayIntent, mMusicConnection,
        // Context.BIND_AUTO_CREATE);
        /* remove callback before click other songs */
        mHandler.removeCallbacks(mUpdateSeekbarTask);
        mCurrentSongId = Integer.parseInt(view.getTag().toString());
        mMusicSrv.setSong(mCurrentSongId);
        Log.i(TAG, "onPlaySong");
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

  public static String getSongPath(Context context, Uri uri) {

      String filePath = null;
      if (uri != null) {
          Cursor cursor = null;
          try {
              String[] proj = {
                  android.provider.MediaStore.Audio.Media.DATA
              };
              cursor = context.getContentResolver().query(uri, proj, null, null, null);
              cursor.moveToFirst();
              filePath = cursor.getString(0);

              if (filePath == null || filePath.trim().isEmpty()) {
                  filePath = uri.toString();
              }
          } catch (Exception e) {
              e.printStackTrace();
              filePath = uri.toString();
          } finally {
              if (cursor != null) {
                  cursor.close();
              }
          }
      } else {
          filePath = uri.toString();
      }
      Log.d(TAG, "path= " + filePath);
      return filePath;
  }

  public static Bitmap getMusicAlbumArt(String filePath) {
      Bitmap bitmap = null;
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      try {
          retriever.setDataSource(filePath);
          byte[] embeddedpic = retriever.getEmbeddedPicture();
          if (embeddedpic != null) {
              bitmap = BitmapFactory.decodeByteArray(embeddedpic, 0, embeddedpic.length);
          }
          if (bitmap == null) {
              return null;
          }
      } catch (Exception ex) {
          ex.printStackTrace();
      } catch (NoSuchMethodError ex) {
          ex.printStackTrace();
      } finally {
          try {
              retriever.release();
          } catch (RuntimeException ex) {
              ex.printStackTrace();
          }
      }
      return bitmap;
  }

    public void checkServiceRunning() {
        if (mMusicSrv == null) {
            mMusicSrvIsRunning = false;
            return;
        } else {
            if (mMusicSrv.isPaused() || mMusicSrv.isPng()) {
                mMusicSrvIsRunning = true;
            }
        }

    }

  public boolean srvIsRunning(){
      return mMusicSrvIsRunning;
  }

  private void setAlbumImage() {
      long id = mMusicSrv.getCurrentSongId();
      Uri musicExternalUri = ContentUris.withAppendedId(MUSIC_URI, id);
      Bitmap albumImage = getMusicAlbumArt(getSongPath(mActivity, musicExternalUri));
      if (albumImage != null) {
          mAlbumImage.setImageBitmap(albumImage);
      } else {
          mAlbumImage.setImageResource(R.drawable.no_album_image);
      }
  }


}
