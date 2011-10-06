package oc.playback;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
	private VideoView videoView;
	
	private int playIndex = 0;
	private ArrayList<String> paths;
	private boolean finished = false;
	
	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.screen);

		paths = getIntent().getStringArrayListExtra(OcPlaybackConstants.VIDEO_FILE_PATHS);
		if(paths==null|| paths.size()==0)
			Toast.makeText(getApplicationContext(), "no video selected", Toast.LENGTH_LONG).show();
			
		videoView = (VideoView) findViewById(R.id.surface_view);
		videoView.setMediaController(new MediaController(this));
		videoView.setOnCompletionListener(new LoopbackListener());
		videoView.setOnClickListener(new ClickListener());
		
		// start
		playVideo(paths.get(0));
	}

	private final class ClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			finished = true;
			videoView.stopPlayback();
			
			Intent result = new Intent();
			result.putExtra(OcPlaybackConstants.VIDEO_SELECTED_PATH, paths.get(playIndex));

			setResult(RESULT_OK, result);
		}
	}

	private final class LoopbackListener implements OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer mp) {
			// release the current resources
			videoView.stopPlayback();

			if(finished==false) { 
				if (++playIndex >= paths.size())
					playIndex = 0;
	
				playVideo(paths.get(playIndex));
			}
		}
	}

	private void playVideo(String path)
	{
		// stop any previous playing, and release resources
		videoView.stopPlayback();
		File movieFile = new File(path);
			
		if(movieFile.exists()) {
			Toast.makeText(getApplicationContext(), "movie does not exist: " + path, Toast.LENGTH_SHORT).show();
			videoView.setVideoPath(path);
			videoView.requestFocus();
			videoView.start();
				
		} else {
			Toast.makeText(getApplicationContext(), "movie does not exist: " + path, Toast.LENGTH_LONG).show();
		}
	}
}
