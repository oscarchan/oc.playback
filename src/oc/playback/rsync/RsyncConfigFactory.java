package oc.playback.rsync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import android.util.Log;

public class RsyncConfigFactory 
{
	public static final String TAG = "RsyncConfigFactory";
	
	private static final String CONF_PATH  = "/etc/rsync.properties";
	
	private static final String TASKS_KEY = "rsync.tasks";

	private List<RsyncConfig> configList;
	
// sample:
//			
// rsync.tasks=event_logs
// rsync.cmd=rsync
//
// event_logs.src.path=ochan@192.168.1.103:~/tmp/test/rsync/test/rsync
// event_logs.dst.path=/data/tmp/test
// event_logs.host.key.path=/etc/ssh_host_key

	private static List<RsyncConfig> readFrom(String file)
	{
		File configFile = new File(file);
		if(configFile.exists()==false || configFile.canRead()==false) {
			Log.e(TAG, "unable to read config: file=" + file 
					+ "; exists=" + configFile.exists()
					+ "; read=" + configFile.canRead()
					);
			return Collections.emptyList();
		}
		
		try {
			Properties properties = new Properties();
			properties.load(new FileReader(configFile));
			return parseProperties(properties);
			
		} catch (IOException e) {
			Log.e(TAG, "unable to parse config: file=" + file, e); 
			return Collections.emptyList();
		}
	}
	
	private static List<RsyncConfig> parseProperties(Properties properties)
	{
		List<RsyncConfig> configList = new ArrayList<RsyncConfig>();
		
		String task_string = properties.getProperty(TASKS_KEY);
		String[] tasks = task_string.split(", ");
		
		for (String task : tasks) {
			RsyncConfig rsyncConfig = new RsyncConfig();
			rsyncConfig.readProperties(task, properties);
			
			configList.add(rsyncConfig);
		}
		
		return configList;
	}

	public static List<RsyncConfig> getRsyncConfig()
	{
//		RsyncConfig defaultConfig = new RsyncConfig()
//			.setTargetPath("/data/tmp/test")
//			.setSourcePath("ochan@192.168.1.103:~/tmp/test/rsync/test/rsync")
//			.setInclusive("*.gz");
//		
//		return Arrays.asList(defaultConfig);
		
		return readFrom(CONF_PATH);
	}
}
