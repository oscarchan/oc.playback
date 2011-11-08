package oc.playback.rsync;

import java.util.Properties;

public class RsyncConfig 
{
	private static final String DEFAULT_PREFIX = "default";

	private static final String SRC_KEY_SUFFIX = ".src.path";
	private static final String DST_KEY_SUFFIX = ".dst.path";
	private static final String INCLUDE_KEY_SUFFIX = ".include";
	private static final String HOST_KEY_SUFFIX = ".host.key.path";
	private static final String CRON_KEY_SUFFIX = ".cron.expr";
	
	private static final String COMMAND_KEY = "rsync.cmd";

	private String taskName;
	
	private String sourcePath;
	private String destPath;
	private String inclusive="*.gz";
	private String hostKeyPath= "/etc/ssh_host_key";
	private Schedule schedule = Schedule.parse("* */5 * * *");  // every 5 min
	private String command = "rsync";
;
	
	public void readProperties(String name, Properties properties) 
	{ 
		this.taskName = name;
		
		sourcePath = getProperty(properties, name, SRC_KEY_SUFFIX);
		destPath = getProperty(properties, name, DST_KEY_SUFFIX);
		hostKeyPath = getProperty(properties, name, HOST_KEY_SUFFIX, hostKeyPath);
		inclusive = getProperty(properties, name, INCLUDE_KEY_SUFFIX);
		String cron_expr = getProperty(properties, name, CRON_KEY_SUFFIX);
		if(cron_expr!=null) { 
			this.schedule = Schedule.parse(cron_expr);
		}
			
		command = properties.getProperty(COMMAND_KEY, command);
	}
	
	private String getProperty(Properties properties, String name, String keySuffix)
	{
		return getProperty(properties, name, keySuffix, null); 
	}
	
	private String getProperty(Properties properties, String name, String keySuffix, String def)
	{
		return properties.getProperty(name + keySuffix, 
				properties.getProperty(DEFAULT_PREFIX + keySuffix, def));
		
	}
	
	public RsyncConfig setInclusive(String inclusivePatthen) {
		this.inclusive = inclusivePatthen;
		return this;
	}
	
	public RsyncConfig setSourcePath(String source) {
		this.sourcePath = source;
		return this;
	}
	
	public RsyncConfig setTargetPath(String target) {
		this.destPath = target;
		return this;
	}
	
	// TODO made it more flexible
	public String getInclusive()
	{
		return inclusive;
	}
	
	public String getSourcePath()
	{
		return sourcePath;
	}
	
	public String getTargetPath()
	{
		return destPath;
	}

	public String getCredentialPath() {
		return hostKeyPath;
	}

	public void setCredentialPath(String credentialPath) {
		this.hostKeyPath = credentialPath;
	}
	
	
	
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule cron) {
		this.schedule = cron;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String toString()
	{
		return "Rcfg[" 
				+ taskName + ", "
				+ "src=" + sourcePath + ", "
				+ "dest=" + destPath + ", "
				+ "key=" + hostKeyPath + ", "
				+ "sch=" + schedule + ", "
				+ "]"
				;
	}
}
