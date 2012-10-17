package oc.playback.rsync;

/**
 * Essentially cron expression
 */ 
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class Schedule
{
	private static final String MINUTE_REGEX = "\\*(/[0-5]?[0-9])?|([0-5]?[0-9])";
	private static final String HOUR_REGEX = "\\*(/[0-2]?[0-9])?|([0-2]?[0-9])";
	
	private String hour;
	private String min;
	
	public static Schedule parse(String expression)
	{
		if(expression==null || expression.trim().isEmpty())
			return null;
		
		String[] split = expression.split(" ");
		if(split.length < 2) {
			Log.w(RsyncConfigFactory.TAG, "unable to parse cron expr: " + expression);
			return null;
		}
		
		Schedule config = new Schedule();
		
		config.min = split[0];
		config.hour = split[1];
		
		if(config.isValid()==false) {  
			Log.w(RsyncConfigFactory.TAG, "invalid cron expr: " + expression);
			return null;
		}
		
		return config;
	}

	public boolean isValid()
	{
		return min.matches(MINUTE_REGEX) && hour.matches(HOUR_REGEX);
	}
	
	public long getRecurringInterval()
	{
		Integer minInterval = getPerInterval(this.min);
		if(minInterval!=null)
			return minInterval * 60 * 1000L;
		else
			return 24 * 60 * 60 * 1000L; // 1 day
	}
	
	public Date nextDate(Date now)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);

		Integer minInterval = getPerInterval(this.min);
		Integer min = getNumber(this.min);
		Integer hour = getNumber(this.hour);
		
		if(isAny(this.hour) && isAny(this.min)) { // e.g. * * * * *  
			cal.add(Calendar.MINUTE, 1); // move to next minute 
		} else if(isAny(this.hour) && minInterval!=null ) {  // e.g. * */5 * * *
			int currMin = cal.get(Calendar.MINUTE);
			int nextMin = (currMin / minInterval + 1) * minInterval;
			cal.add(Calendar.MINUTE, nextMin - currMin);
		} else if (hour!=null && min!=null) {  // e.g. 22 0 * *  *
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, min);
			
			if(cal.getTime().after(now)==false)  
				cal.add(Calendar.DAY_OF_YEAR, 1);
		} else {
			// not knowing what to do
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		return now;
	}
	
	private static boolean isAny(String expr)
	{
		return expr.matches("\\*");
	}

	private static Integer getPerInterval(String expr)
	{
		String[] split = expr.split("/");
		
		if(split.length==2)
			return getNumber(split[1]);
		
		return 1;
	}
	
	private static Integer getNumber(String expr)
	{
		try { 
			return Integer.parseInt(expr);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "schd[" + min + " " + hour + "]";
	}
	
}
