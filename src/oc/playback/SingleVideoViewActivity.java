package oc.playback;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * Leverage the VideoView for a single video view in loop
 * @author ochan
 *
 */
public class SingleVideoViewActivity extends Activity 
{
	private VideoView videoView;

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.screen);
		videoView = (VideoView) findViewById(R.id.surface_view);
		TextView errorTextView = (TextView) findViewById(R.id.error_msg);

//		String path = "/sdcard/Video/(class 2 of 6) Efficient Rails Test Driven Development - by Wolfram Arnold_HD.mp4";
//		String path = "/mnt/sdcard/Video/kung-fu-panda-2.mp4";
		String path = "/mnt/sdcard/Video/test.mp4";
		
		File clip = new File(path);
//		File clip=new File(Environment.getExternalStorageDirectory(),
//                       "/Video/kung-fu-panda-2.mp4");
		
		if(clip.exists()) {
			String errorText =  "clip exists";
			
			errorTextView.setText(errorText);
			videoView.setVideoPath(clip.getAbsolutePath());
			videoView.setMediaController(new MediaController(this));
			videoView.requestFocus();
			videoView.start();
		} else {
			String parentPath = clip.getParentFile().getParent();

			List<String> files = Arrays.asList(clip.getParentFile().list());
			
			
			errorTextView.setText("files: " + files);
		}
	}
}
