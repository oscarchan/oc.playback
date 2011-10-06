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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OcPlaybackActivity extends Activity  
{
	private int OC_PLAYBACK_CODE = 5;
	
    /** Called when the activity is first created. */
	private static List<String> SUPPORTED_VIDEO_TYPES = Arrays.asList("mp4");
	
	private String[] videoNames;
	private File videoDir;
	
	private ListView listView;

	@Override
	protected void onCreate(Bundle state) 
	{
		super.onCreate(state);
		setContentView(R.layout.main); 
		
		videoDir = Environment.getExternalStoragePublicDirectory("Video");
		videoNames = listVideos(videoDir);
		
		TextView videoDirView = (TextView) this.findViewById(R.id.clip_path);
		videoDirView.setText(videoDir.getAbsolutePath());
		
		listView = (ListView) this.findViewById(R.id.clip_name_list_view);
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, videoNames));
				
		setUpViewButton();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode==RESULT_OK && data.getExtras()!=null) {
			int playIndexSelected = data.getExtras().getInt("video.path.index");
			
			Toast.makeText(getApplicationContext(), "play index selected: " + playIndexSelected, Toast.LENGTH_LONG).show();
		} else { 
			Toast.makeText(getApplicationContext(), "child view returned error, cancelled, or missing selected video: result_code=" + resultCode, Toast.LENGTH_LONG).show();
			
		}
	}
    private void setUpViewButton()
    {
        Button viewButton = (Button) this.findViewById(R.id.video_view_selection);
        
        viewButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            	SparseBooleanArray checkedPositions = listView.getCheckedItemPositions();
                Intent intent = new Intent(OcPlaybackActivity.this, VideoViewActivity.class);
                
                ArrayList<String> videoPaths = getVideoPaths(checkedPositions);
                intent.putStringArrayListExtra(OcPlaybackConstants.VIDEO_FILE_PATHS, videoPaths);
                startActivityForResult(intent, OC_PLAYBACK_CODE);
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
		
		return videoNames;
	}
	
}