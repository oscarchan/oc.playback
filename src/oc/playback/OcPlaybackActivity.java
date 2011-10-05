package oc.playback;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

public class OcPlaybackActivity extends Activity 
{
    /** Called when the activity is first created. */
	private static List<String> SUPPORTED_MOVIE_TYPES = Arrays.asList("mp4");
	
	private String[] movieNames;
//	private List<CheckedTextView> movieNames;
	private File movieDir;
	
	private ListView listView;
	
	
//	@Override
//	protected void onCreate(Bundle state) 
//	{
//		super.onCreate(state);
//		movieDir = Environment.getExternalStoragePublicDirectory("Video");
//		movieNames = listMovies(movieDir);
//		
//		TextView clipPathView = (TextView) this.findViewById(R.id.clip_path);
//		
//		ListView listView = (ListView) this.findViewById(R.id.clip_name_list_view);
//		
//		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, movieNames));
//		
//		listView.setTextFilterEnabled(true);
//		
//		listView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				Intent intent = new Intent(OcPlaybackActivity.this, SingleVideoViewActivity.class);
//				
//
//				Object selected = getListView().getSelectedItem();
//				
//				String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/" + selected;
//
//				startActivityForResult(intent, 0);				
//			}
//		});
//		
//		
//	}

	
	@Override
	protected void onCreate(Bundle state) 
	{
		super.onCreate(state);
		setContentView(R.layout.main); 
		
		movieDir = Environment.getExternalStoragePublicDirectory("Video");
		movieNames = listMovies(movieDir);
//		movieNames = checkTestViewList(listMovies(movieDir));
		
		TextView clipPathView = (TextView) this.findViewById(R.id.clip_path);
		clipPathView.setText(movieDir.getAbsolutePath());
		
		listView = (ListView) this.findViewById(R.id.clip_name_list_view);
		
//		listView.setAdapter(new ArrayAdapter<String>(this, R.layout.checkable_text, movieNames));
		listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, movieNames));
//		listView.setAdapter(new ArrayAdapter<CheckedTextView>(this, R.layout.checkable_text, movieNames));
				
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				CheckedTextView textView = (CheckedTextView) view;
//				
//				String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/" + textView.getText();
//				Intent intent = new Intent(OcPlaybackActivity.this, SingleVideoViewActivity.class);
//
//				intent.setData(new Uri.Builder().scheme("file").appendPath(path).build());
//				startActivityForResult(intent, 0);				
			}
		});
		
		
	}
	
	private String[] listMovies(File movieDir) {

		String[] movieNames = movieDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				for (String type : SUPPORTED_MOVIE_TYPES) {
					if(filename.endsWith(type))
						return true;
				}
				
				return false;
			}
		});
		
		return movieNames;
	}
	
	private List<CheckedTextView> checkTestViewList(String[] texts) 
	{
		List<CheckedTextView> list = new ArrayList<CheckedTextView>(texts.length);
		
		for (String text : texts) {
			CheckedTextView textView = new CheckedTextView(this.getApplicationContext());
			textView.setText(text);
			list.add(textView);
		}
		
		return list;
	}
}