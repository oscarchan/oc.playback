package oc.playback.rsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class RsyncService extends Service
{
	private static final String TAG = "RsyncService";
	
	private RsyncTask task;
	private List<RsyncConfig> configList;
	
	private Executor executor;
	private ArrayList taskList;
	
	private IBinder binder = new RsyncBinder();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		Log.i(TAG, "on start command: instance=" + this);
		
		rsync();
		
		return Service.START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "on start: instance=" + this);
		super.onStart(intent, startId);
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "created: instance=" + this);
		super.onCreate();
		
		configList = RsyncConfigFactory.getRsyncConfig();
		task = new RsyncTask();
		
		executor = Executors.newSingleThreadExecutor();
		
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "created: instance=" + this);
		super.onDestroy();
	}

	public void rsync()
	{
		task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, configList.toArray(new RsyncConfig[0]));
		
	}
	
	// TODO HIGH ochan move to a different thread 
	private static final String RSYNC_COMMAND_TEMPLATE = "/data/tmp/rsync -Pavv -e 'ssh -y -i %s' --times --include '%s' %s %s >> %s 2>&1";
	// -e "ssh -i /path_to_your_private_key" 
	private void rsync(RsyncConfig config) 
	{
		Log.i(TAG, "rsync(" + config + ")");
		
		File destDir = new File(config.getTargetPath());
		
		if(destDir.exists()==false) { 
			destDir.mkdir();
		}
			
		if( destDir.isDirectory()==false) { 
			Log.e(TAG, "invalid dest path: " + config.getTargetPath());
			return;
		}
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		String timestamp = format.format(new Date());
		String command = String.format(RSYNC_COMMAND_TEMPLATE,
				config.getCredentialPath(),
				config.getInclusive(), config.getSourcePath(), config.getTargetPath(),
				RsyncConstants.RSYNC_LOG_DIR +  "/rsync." + timestamp  +".log");
	
		try {
			Log.i(TAG, "starting: " + command);
			List<String> results = execute(command);
			for (String line: results) {
				Log.d(TAG, "rsync log: " + line);
			}
			Log.i(TAG, "finished: " + command);
		} catch (IOException e) { 
			Log.e(TAG, "rsync: io issues: cmd", e);
		} catch (InterruptedException e) {
			Log.e(TAG, "rsync: interrupted: cmd=" + command, e );
		}
	}
	
	private static List<String> execute(String command) throws IOException, InterruptedException
	{
		List<String> result = new ArrayList<String>();
		
		// creating a process to handle rsync command
		Process process = Runtime.getRuntime().exec("su");
		// note: change it to use writer instead of stream 
		// note: remove DataOutstream to see if it really needs
		Writer writer = new OutputStreamWriter(process.getOutputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		
		writer.write(command + "\n");
		writer.write("exit\n");
		writer.flush();
		String line;
		while((line = reader.readLine())!=null) {
			result.add(line);
		}

		process.waitFor();

		return result;
	}

	public class RsyncBinder extends Binder {
        RsyncService getService() {
            return RsyncService.this;
        }
    }

	class RsyncRunable implements Runnable
	{

		@Override
		public void run() {
			Log.i(TAG, "RsyncRunable started: " + configList);
			
			for (RsyncConfig config : configList) {
				rsync(config);
			}
			
			Log.i(TAG, "RsyncRunable ended");
		}
		
	}
	class RsyncTask extends AsyncTask<RsyncConfig, Integer, Integer>
	{

		@Override
		protected Integer doInBackground(RsyncConfig... config) {
			Log.i(TAG, "RsyncTask started: " + config.length);
			for (int i=0;i<config.length;i++) {
				RsyncConfig cfg = config[i];
				rsync(cfg);
			}
			
			Log.i(TAG, "RsyncTask ended");
			
			return 0;
		}
		
	}
	
}
