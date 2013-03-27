package edu.mayo.informatics.cntro.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.utils.CNTROUtils;

public class TimeInterval extends Time
{
	private Time startTime;
	private Time endTime;
	private Duration duration;
	
	public TimeInterval(TimeAssemblyMethod method) 
	{
		super(method);
		this.startTime = null;
		this.endTime = null;
		this.duration = null;
	}
	
	public TimeInterval(Time startTime, Time endTime, Duration duration, TimeAssemblyMethod method) 
	{
		super(method);
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		
		if (method == null)
		{
			if((startTime == null)&&(endTime == null)&&(duration == null))
				this.assemblyMethod = TimeAssemblyMethod.UNKNOWN;
			else
				this.assemblyMethod = TimeAssemblyMethod.ASSERTED;
		}
		else
			this.assemblyMethod = method;
	}

	public Time getStartTime() 
	{
		if (this.startTime == null)
			if ((this.endTime != null)&&(this.duration != null))
				computeStartTime();

		return startTime;
	}

	private void computeStartTime()
	{
		Date endDate = CNTROUtils.getDateFromTime(this.endTime);
		if (endDate != null)
		{
			Calendar cal = Calendar.getInstance(Locale.US);
			cal.setTime(endDate);
			int calendarUnit = Event.getCalendarGranularityFromEventGranularity(this.duration.unit);
			cal.add(calendarUnit, (int) (this.duration.value * (-1)));
			Date strtDate =  cal.getTime();
			Granularity  g = Event.getEventGranularityFromCalendarGranularity(calendarUnit);
			TimeInstant ti = new TimeInstant(strtDate.toString(), false, g, strtDate.toString(), strtDate, TimeAssemblyMethod.INFERRED);
			this.startTime = ti;
		}
	}
	
	public void setStartTime(Time startTime) 
	{
		this.startTime = startTime;
	}

	public Time getEndTime() 
	{
		if (this.endTime == null)
			if ((this.startTime != null)&&(this.duration != null))
				computeEndTime();

		return endTime;
	}

	private void computeEndTime()
	{
		Date strtDate = CNTROUtils.getDateFromTime(this.startTime);
		if (strtDate != null)
		{
			Calendar cal = Calendar.getInstance(Locale.US);
			cal.setTime(strtDate);
			int calendarUnit = Event.getCalendarGranularityFromEventGranularity(this.duration.unit);
			cal.add(calendarUnit, (int) (this.duration.value));
			Date endDt =  cal.getTime();
			Granularity  g = Event.getEventGranularityFromCalendarGranularity(calendarUnit);
			TimeInstant ti = new TimeInstant(endDt.toString(), false, g, endDt.toString(), endDt, TimeAssemblyMethod.INFERRED);
			this.endTime = ti;
		}
	}

	public void setEndTime(Time endTime) 
	{
		this.endTime = endTime;
	}

	public Duration getDuration() 
	{
		return duration;
	}

	public void setDuration(Duration duration) 
	{
		this.duration = duration;
	}

	public String toString()
	{
		return "{" + super.toString() + 
		((this.startTime != null)? ("{Start:" + this.startTime + "}"):"")  +
		((this.endTime != null)? ("{End:" + this.endTime + "}"):"")  +
		((this.duration != null)? ("{Dur:" + this.duration + "}"):"") + "}";
	}
	
	public String getClsId()
	{
		return ((this.startTime != null)? (this.startTime.getClsId()):"")  +
		((this.endTime != null)? (this.endTime.getClsId()):"")  +
		((this.duration != null)? (this.duration.getClsId()):"");
	}
}
