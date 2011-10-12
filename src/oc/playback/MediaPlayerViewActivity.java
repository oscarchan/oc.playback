package oc.playback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class MediaPlayerViewActivity extends Activity
{
	private static final String TAG = "MediaPlayerViewActivity"; 
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private MediaPlayer mediaPlayer;
	
	private int playIndex = 0;
	private ArrayList<String> paths;
	private boolean finished = false;
	
	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		
		surfaceView = new SurfaceView(this);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(new HolderCallback());
	
		paths = getIntent().getStringArrayListExtra(OcPlaybackConstants.VIDEO_FILE_PATHS);
		if(paths==null|| paths.size()==0)
			Toast.makeText(getApplicationContext(), "no video selected", Toast.LENGTH_LONG).show();
		
		setContentView(surfaceView);
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setDisplay(surfaceHolder);
		mediaPlayer.setOnBufferingUpdateListener(new BufferUpdateListener());
		mediaPlayer.setOnCompletionListener(new CompletionListener());
		mediaPlayer.setOnPreparedListener(new PreparedListener());
		mediaPlayer.setOnVideoSizeChangedListener(new VideoSizeChangedListener());		
	}
	
	private void playVideo(String path)
	{
		// TODO stop 
		
		File movieFile = new File(path);
			
		if(movieFile.exists()) {
			try { 
				mediaPlayer.setDataSource(path);
				mediaPlayer.prepare();
				Toast.makeText(getApplicationContext(), "movie is playing: " + path, Toast.LENGTH_SHORT).show();
			} catch (IOException e ) {
				Log.e(TAG, "unable to play video: " + path + ": " + e, e);
			}
		} else {
			Toast.makeText(getApplicationContext(), "movie does not exist: " + path, Toast.LENGTH_LONG).show();
		}		
	}
	
	private void stopVideo()
	{
		
	}


	private final class VideoSizeChangedListener implements
			OnVideoSizeChangedListener {
		@Override
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			// TODO Auto-generated method stub
			
		}
	}


	private final class PreparedListener implements OnPreparedListener {
		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			
		}
	}


	private final class CompletionListener implements OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			
		}
	}


	private final class BufferUpdateListener implements
			OnBufferingUpdateListener {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			// TODO Auto-generated method stub
			
		}
	}


	public class HolderCallback implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
	        Log.d(TAG, "surfaceChanged called" + holder);
		}
	
	
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
	        Log.d(TAG, "surfaceCreated called: " + holder);
		}
	
	
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
	        Log.d(TAG, "surfaceDestored called" + holder);
		}
	
	
	}
}
