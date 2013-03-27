package edu.mayo.informatics.cntro.model;

import java.util.Calendar;
import java.util.Date;

import edu.mayo.informatics.cntro.queryIF.Granularity;

public class NormalizedDate 
{
	public Date normalizedDate;
	public int calendarGranularity = Calendar.DATE;
	
	public Granularity getNormalizedGranularity()
	{
		switch(calendarGranularity)
		{
			case Calendar.YEAR : return Granularity.YEAR; 
			case Calendar.MONTH : return Granularity.MONTH;
			default : return Granularity.DAY;
		}
	}
}
