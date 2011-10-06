package oc.playback;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OcPlaybackActivity extends Activity  
{
	private final class EnterKeyListener implements OnKeyListener {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			switch(event.getKeyCode()) { 
				case KeyEvent.KEYCODE_ENTER:
				{
					CharSequence dir = videoDirView.getText();
					Toast.makeText(getApplicationContext(), "video dir: " + dir, Toast.LENGTH_LONG).show();
					videoDir = new File(dir.toString());
					refreshFiles();
					return true;
				}
				default:
				{
					return false;
				}
			}
		}
	}

	private int OC_PLAYBACK_CODE = 5;
	
    /** Called when the activity is first created. */
	private static List<String> SUPPORTED_VIDEO_TYPES = Arrays.asList("mp4");
	
	private String[] videoNames;
	private File videoDir;
	
	private ListView listView;
	private TextView videoDirView;
	
	@Override
	protected void onCreate(Bundle state) 
	{
		super.onCreate(state);
		setContentView(R.layout.main); 
		
		videoDir = Environment.getExternalStoragePublicDirectory("Video");
		videoDirView = (TextView) this.findViewById(R.id.clip_path);
		videoDirView.setText(videoDir.getAbsolutePath());
		videoDirView.setOnKeyListener(new EnterKeyListener());
		

		refreshFiles();
		setUpViewButtons();
	}
	
	private void refreshFiles()
	{
		videoNames = listVideos(videoDir);
		listView = (ListView) this.findViewById(R.id.clip_name_list_view);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, videoNames));
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode==RESULT_OK && data.getExtras()!=null) {
			String selectedVideoPath = data.getExtras().getString(OcPlaybackConstants.VIDEO_SELECTED_PATH);
			
			Toast.makeText(getApplicationContext(), "video file selected: " + selectedVideoPath, Toast.LENGTH_LONG).show();
		} else { 
			Toast.makeText(getApplicationContext(), "child view returned error, cancelled, or missing selected video: result_code=" + resultCode, Toast.LENGTH_LONG).show();
			
		}
	}
    private void setUpViewButtons()
    {
        Button viewButton = (Button) this.findViewById(R.id.video_view_selection);
        
        viewButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            	SparseBooleanArray checkedPositions = listView.getCheckedItemPositions();
                
                ArrayList<String> videoPaths = getVideoPaths(checkedPositions);
                
                if(videoPaths.isEmpty())  {
                	Toast.makeText(getApplicationContext(), "no videos are selected", Toast.LENGTH_LONG).show();
                } else { 
                	Intent intent = new Intent(OcPlaybackActivity.this, VideoViewActivity.class);
                	intent.putStringArrayListExtra(OcPlaybackConstants.VIDEO_FILE_PATHS, videoPaths);
                	startActivityForResult(intent, OC_PLAYBACK_CODE);
                }
            }
        });
        
        viewButton = (Button) this.findViewById(R.id.media_player_selection);
        
        viewButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(OcPlaybackActivity.this, VideoViewActivity.class);
                startActivity(intent);
            }
        });
        
    }
	
	
	private ArrayList<String> getVideoPaths(SparseBooleanArray checkedPositions) {
		ArrayList<String> videoPaths = new ArrayList<String>();
		for (int i = 0; i < videoNames.length; i++) {
			if(checkedPositions.get(i))
				videoPaths.add(videoDir.getAbsolutePath() + "/" + videoNames[i]);
		}
		
		return videoPaths;
	}

	private String[] listVideos(File videoDir) {
		
		String[] videoNames = videoDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				for (String type : SUPPORTED_VIDEO_TYPES) {
					if(filename.endsWith(type))
						return true;
				}
				
				return false;
			}
		});
		
		if(videoNames==null)
			return new String[0];
		
		return videoNames;
	}
	
}