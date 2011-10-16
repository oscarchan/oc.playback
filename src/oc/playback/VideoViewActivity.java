package oc.playback;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * Leverage the VideoView to handle loop
 * @author ochan
 *
 */
public class VideoViewActivity extends Activity 
{
	private static final boolean DEBUG = true;
	private static final long ONE_SECOND = 1000L;
	private static final int PROGRESS_PRECISION = 1000;

	private final static String TAG = "VideoViewActivity";
	
	private VideoView videoView;
	private TextView errorMessage;
	private MediaPlayerControl videoControl;
	
	private int playIndex = 0;
	private ArrayList<String> paths;
	private boolean looping;
	private boolean controllerVisible;
	
	private int errorCounts = 0;
	
	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.screen);

		paths = getIntent().getStringArrayListExtra(OcPlaybackConstants.VIDEO_FILE_PATHS);
		looping = getIntent().getBooleanExtra(OcPlaybackConstants.VIDEO_LOOPING, false);
		controllerVisible = getIntent().getBooleanExtra(OcPlaybackConstants.VIDEO_CONTROL_VISIBLE, false);
		
		if(paths==null|| paths.size()==0)
 			Toast.makeText(getApplicationContext(), "no video selected", Toast.LENGTH_LONG).show();
			
		videoView = (VideoView) findViewById(R.id.surface_view);
		errorMessage = (TextView) findViewById(R.id.error_message);
		videoControl = videoView;
		
		videoView.setOnErrorListener(new VideoErrorListener());
		
		if(controllerVisible) {
			setUpController();
		} else { 
			videoView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getAction()==MotionEvent.ACTION_MOVE && event.getX() < 50.0 && event.getY() < 50.0)  
						return false;
	
					stopVideo();
					
					return true;
				}
			});			
		}
		
		if(looping) { 
			videoView.setOnCompletionListener(new LoopbackListener());
		} 
		
		// start
		playVideo(paths.get(0));
	}

	private final class VideoErrorListener implements OnErrorListener {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			errorCounts++;
			Log.e(TAG, "video error: count=" + errorCounts + ": what=" + what + ": extra=" + extra + ": " + paths.get(playIndex));
 
			if(DEBUG) {
				errorMessage.setText("count=" + errorCounts + "; last=" + what);
				errorMessage.setVisibility(View.VISIBLE);
			}

			// replay the server with delay
			try { 
				// delay for 0.5 sec
				Thread.sleep(500);
			}catch (InterruptedException e) { 
				// nothing
			}
			
			if(videoView.isPlaying()) { 
				Log.i(TAG, "onError: stop playing: =" + paths.get(playIndex));
				videoView.stopPlayback();
			}
			
			playVideo(paths.get(playIndex));
			return true;
		}
	}

	private final class LoopbackListener implements OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer mp) {
			// release the current resources
			if(videoView.isPlaying()) {
				Log.i(TAG, "onCompletion: stop playing: =" + paths.get(playIndex));
				videoView.stopPlayback();
			}
			
			if(looping) { 
				if (++playIndex >= paths.size())
					playIndex = 0;
	
				playVideo(paths.get(playIndex));
			}
		}
	}

	private void stopVideo() {
		looping = true;
		
		if(videoView.isPlaying())  {
			Log.d(TAG, "stopVideo: stop playing: =" + paths.get(playIndex));
			videoView.stopPlayback();
		}
		
		Intent result = new Intent();
		result.putExtra(OcPlaybackConstants.VIDEO_SELECTED_PATH,
				paths.get(playIndex));

		setResult(RESULT_OK, result);

		if(isFinishing()==false) {
			Log.d(TAG, "stopVideo: finish()");
			finish();
		}
	}

	private void playVideo(String path)
	{
		// pre-con'd: any previous playing video are stop, and resources are released

		File movieFile = new File(path);
			
		if(movieFile.exists()) {
			videoView.setVideoPath(path);
			if(videoView.isFocused()==false)
				videoView.requestFocus();
			
			Log.d(TAG, "playVideo: " + path);
			// TODO OCHAN HIGH need a better way to notify
			int duration = videoView.getDuration();
			TextView timeTotal = (TextView) findViewById(R.id.time_total);
			timeTotal.setText(String.valueOf(duration));
			videoView.start();
				
			Toast.makeText(getApplicationContext(), "movie is playing: " + path, Toast.LENGTH_SHORT).show();
		} else {
			Log.w(TAG, "playVideo: file not found " + path); 
			Toast.makeText(getApplicationContext(), "movie does not exist: " + path, Toast.LENGTH_LONG).show();
		}
		
		handler.sendEmptyMessageDelayed(SHOW_PROGRESS, ONE_SECOND);
	}
	
	// UI control logic
	private void setUpController() {
		View controller = findViewById(R.id.controller);
		controller.setVisibility(View.VISIBLE);
		
		pauseBtn = (ImageView) findViewById(R.id.pause);
		progress  = (SeekBar) findViewById(R.id.progress);
		currentTime = (TextView) findViewById(R.id.time_current);
		totalTime = (TextView) findViewById(R.id.time_total);
		
		pauseBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopVideo();
			}
		});
		
		progress.setOnSeekBarChangeListener(seekListener);
        progress.setMax(PROGRESS_PRECISION);
	}
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case SHOW_PROGRESS:
                    pos = setProgress();
//                    Log.d(TAG, "received: playing=" + videoControl.isPlaying());
                    
                    if (!dragging && videoControl.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        // always at the start of next second
                        sendMessageDelayed(msg, ONE_SECOND - (pos % PROGRESS_PRECISION));
                    }
                    break;
            }
        }
    };
    

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private OnSeekBarChangeListener seekListener = new OnSeekBarChangeListener() {
        long duration;
        public void onStartTrackingTouch(SeekBar bar) {
            duration = videoControl.getDuration();
        }
        public void onProgressChanged(SeekBar bar, int progress, boolean fromtouch) {
        	
            if (fromtouch) {
            	Log.d(TAG, "onProgressChanged started: progress=" + paths.get(playIndex)+ progress + "; tounch=" + fromtouch) ;
                dragging = true;
                duration = videoControl.getDuration();
                long newposition = (duration * progress) / 1000L;
                videoControl.seekTo( (int) newposition);
                if (currentTime != null)
                    currentTime.setText(stringForTime( (int) newposition));
                
                dragging = false;
                Log.d(TAG, "onProgressChanged ended") ;
            }
        }
        public void onStopTrackingTouch(SeekBar bar) {
            dragging = false;
            setProgress();
            togglePause();
        }
    };

    
    private int setProgress() {
        if (videoControl==null || dragging) {
            return 0;
        }
        int position = videoControl.getCurrentPosition();
        int duration = videoControl.getDuration();
        if (progress!= null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                progress.setProgress( (int) pos);
            }
            int percent = videoControl.getBufferPercentage();
            progress.setSecondaryProgress(percent * 10);
        }

        if (totalTime != null)
            totalTime.setText(stringForTime(duration));
        
        if (currentTime != null)
            currentTime.setText(stringForTime(position));

        return position;
    }
    
    private void togglePause() {
        if (videoControl.isPlaying()) {
            pauseBtn.setImageResource(R.drawable.done_active);
        } else {
            pauseBtn.setImageResource(R.drawable.done_inactive);
        }
    }

    private static final int    SHOW_PROGRESS = 2;

    private boolean dragging;
    private ImageView pauseBtn;
    private SeekBar progress;
    private TextView currentTime;
    private TextView totalTime;
}
