package edu.mayo.informatics.cntro.test;

import java.util.Date;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.impl.CNTROQueryImpl;
import edu.mayo.informatics.cntro.main.CNTROAuxiliary;
import edu.mayo.informatics.cntro.model.Duration;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.model.Time;
import edu.mayo.informatics.cntro.model.TimeInstant;
import edu.mayo.informatics.cntro.model.TimeInterval;
import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.utils.CNTROUtils;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.Period;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.exceptions.TemporalException;

public class KimCNTROQueryImpl extends CNTROQueryImpl 
{
	public KimCNTROQueryImpl(CNTROAuxiliary pAux) 
	{
		super(pAux);
	}

	@Override
	public Duration getDurationBetweenEvents(Event startEvent, Event endEvent,
			Granularity granularity) throws CNTROException 
	{
		try
		{
				if (startEvent == null)
					throw new CNTROException("Can't determine duration as start time is null.");

				if (endEvent == null)
					throw new CNTROException("Can't determine duration as start time is null.");

				Date endOf1 = startEvent.findEventEndTime();
				Date startOf2 = endEvent.findEventStartTime();
				
				Time endTime = null;
				
				if (endOf1 == null)
				{
					endTime = startEvent.getTime(true);
					
					if ((endTime != null)&&(endTime instanceof TimeInstant))
					{
						endOf1 = ((TimeInstant) endTime).getNormalizedTime();
					}
				}

				Time strTime = null;
				if (startOf2 == null)
				{
					 strTime = endEvent.getTime(true);
					
					if ((strTime != null)&&(strTime instanceof TimeInstant))
					{
						startOf2 = ((TimeInstant) strTime).getNormalizedTime();
					}
				}

				if ((endOf1 != null)&&(startOf2 != null))
				{			
					Granularity gran1 = Granularity.DAY;
					Granularity gran2 = Granularity.DAY;
				
					if (endTime != null)
						gran1 = endTime.granularity;
					else
						gran1 = startEvent.getGranularity();
					
					if (strTime != null)
						gran2 = strTime.granularity;
					else
						gran2 = endEvent.getGranularity();

					int ydiff = (startOf2.getYear() - endOf1.getYear());
			
					Duration dd = new Duration("Computed by program", -1, Granularity.MONTH);
					if ((granularity == Granularity.MONTH)&&(gran1 == Granularity.YEAR)&&(gran2 == Granularity.YEAR))
					{
						if ( ydiff > 0)
						{
							dd.value = Math.abs(((ydiff - 1)*12) + 6);
						}
						else
							dd.value = 6;
						
						return dd;
					}
					
					if ( ydiff > 0)
					{
						dd.value = ydiff;
						return dd;
					}
					
					if ((granularity == Granularity.MONTH)&&(gran1 == Granularity.DAY)&&(gran2 == Granularity.DAY))
					{
						dd = super.getDurationBetweenEvents(startEvent, endEvent, Granularity.DAY);
						
						if (dd.value > 0)
						{
							dd.value = (dd.value/30) + (((dd.value%30) > 14)?1:0);
							return dd;
						}
					}
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Duration dur = super.getDurationBetweenEvents(startEvent, endEvent, granularity);
		
		//if (dur != -1)
			//return getKimModifiedGranularityInMonths(dur, startEvent.getGranularity(), true);
		
		return dur;
	}

	public long getDuration(Event event, boolean adjustForKimsCalculaiton) throws CNTROException 
	{
		Duration d = getDuration(event);
		
		if (d != null)
			return KimCNTROQueryImpl.getKimModifiedGranularityInMonths(d.value, d.unit, adjustForKimsCalculaiton);
		
		throw (new CNTROException("Duration is null!!"));
	}
	
	public Duration getDuration(Event event) throws CNTROException 
	{
		if (event != null)
		{
			try
			{
				return super.getDuration(event);
			}
			catch(Exception ce2)
			{
				Time st = event.getTime(true);
				
				if ((st != null)&&(st instanceof TimeInterval))
				{
					TimeInterval sti = (TimeInterval)st;
					return sti.getDuration();
				}
				
				throw new CNTROException(ce2.getLocalizedMessage());
			}
		}

		throw (new CNTROException("Event is null!!"));
	}

	public Duration convertValueBasedOnGranularity(Period period, Granularity fromGranularity, Granularity toGranularity) throws TemporalException
	{
		if ((fromGranularity == null)||(toGranularity == null))
			return super.convertValueBasedOnGranularity(period, fromGranularity, toGranularity);
		
		
		Granularity toAdjusted = toGranularity;
		Granularity fromAdjusted = fromGranularity;
		
		if (toGranularity == Granularity.WEEK)
			toAdjusted = Granularity.DAY;
		
		if (fromGranularity == Granularity.WEEK)
			fromAdjusted = Granularity.DAY;

		int fromTemGranularity = CNTROUtils.getTemporalGranularityFromTime(fromAdjusted);
		int toTempGranularity = CNTROUtils.getTemporalGranularityFromTime(toAdjusted);
	    
		long finalValue = -1;
		if (fromTemGranularity != toTempGranularity)
		{
			long computedValue = period.duration(fromTemGranularity);
			finalValue = KimCNTROQueryImpl.getKimModifiedGranularityInMonths(computedValue, fromAdjusted, false);
			return new Duration("Compouted after adjustment", (int) finalValue, Granularity.MONTH);
		}
		
		return new Duration("Compouted after adjustment", (int) period.duration(toTempGranularity), Granularity.MONTH);
	}
	
	public static long getKimModifiedGranularityInMonths(long value, Granularity fromGranularity, boolean adjust)
	{
		if (fromGranularity == Granularity.YEAR)
		{
			if ( value > 0)
			{
				if (!adjust)
					return (value*12);
				
				return Math.abs(((value - 1)*12) + 6);
			}
			else
				if ((value == 0)&&(adjust))
					return 6;
		}
		
		if ((fromGranularity == Granularity.DAY)||(fromGranularity == Granularity.UNKNOWN))
		{
			if (value > 0)
			{
				//if (!adjust)
				//	return (value/30);

				return (value/30) + (((value%30) > 14)?1:0);
			}
		}
		
		if (fromGranularity == Granularity.WEEK)
		{
			if (value > 0)
			{
				return (value/4);
			}
		}
		
		return value;
	}
	
}
