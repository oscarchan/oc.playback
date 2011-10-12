package oc.playback;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * Leverage the VideoView to handle loop
 * @author ochan
 *
 */
public class VideoViewActivity extends Activity 
{
	private final static String TAG = "VideoViewActivity";
	
	private VideoView videoView;
	
	private int playIndex = 0;
	private ArrayList<String> paths;
	private boolean looping;
	private boolean controlVisible;
	
	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.screen);

		paths = getIntent().getStringArrayListExtra(OcPlaybackConstants.VIDEO_FILE_PATHS);
		looping = getIntent().getBooleanExtra(OcPlaybackConstants.VIDEO_LOOPING, false);
		controlVisible = getIntent().getBooleanExtra(OcPlaybackConstants.VIDEO_CONTROL_VISIBLE, false);
		
		if(paths==null|| paths.size()==0)
 			Toast.makeText(getApplicationContext(), "no video selected", Toast.LENGTH_LONG).show();
			
		videoView = (VideoView) findViewById(R.id.surface_view);
		videoView.setOnErrorListener(new VideoErrorListener());
		

		if(controlVisible) { 
			MediaController mediaController = new MediaController(this);
			videoView.setMediaController(mediaController);
		}
		
		if(looping) { 
			videoView.setOnCompletionListener(new LoopbackListener());
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
		
		// start
		playVideo(paths.get(0));
	}


	private final class VideoErrorListener implements OnErrorListener {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.e(TAG, "video error: what=" + what + ": extra=" + extra + ": " + paths.get(playIndex));
			Toast.makeText(getApplicationContext(), "video error: what=" + what + ": extra=" + extra, Toast.LENGTH_LONG).show();
			// do nothing, let's see if it continue to play
			
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
			Log.i(TAG, "stopVideo: stop playing: =" + paths.get(playIndex));
			videoView.stopPlayback();
		}
		
		Intent result = new Intent();
		result.putExtra(OcPlaybackConstants.VIDEO_SELECTED_PATH,
				paths.get(playIndex));

		setResult(RESULT_OK, result);

		if(isFinishing()==false) {
			Log.i(TAG, "stopVideo: finish()");
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
			
			Log.i(TAG, "playVideo: " + path); 
			videoView.start();
				
			Toast.makeText(getApplicationContext(), "movie is playing: " + path, Toast.LENGTH_SHORT).show();
		} else {
			Log.w(TAG, "playVideo: file not found " + path); 
			Toast.makeText(getApplicationContext(), "movie does not exist: " + path, Toast.LENGTH_LONG).show();
		}
	}
	
}
