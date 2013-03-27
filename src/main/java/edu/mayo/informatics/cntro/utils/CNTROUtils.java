package edu.mayo.informatics.cntro.utils;


import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.model.CNTROCls;
import edu.mayo.informatics.cntro.model.CNTROModelConstants;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.model.EventComparison;
import edu.mayo.informatics.cntro.model.TemporalRelation;
import edu.mayo.informatics.cntro.model.Time;
import edu.mayo.informatics.cntro.model.TimeAssemblyMethod;
import edu.mayo.informatics.cntro.model.TimeInstant;
import edu.mayo.informatics.cntro.model.TimeInterval;
import edu.mayo.informatics.cntro.model.TimePeriod;
import edu.mayo.informatics.cntro.model.TimePhase;
import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.queryIF.TemporalRelationType;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.Instant;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.JDBCDatetimeStringProcessor;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.Period;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.Temporal;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.exceptions.TemporalException;

public class CNTROUtils 
{
	public static boolean isNull(String str)
	{
		if ((str == null)||("null".equalsIgnoreCase(str.trim()))||("".equalsIgnoreCase(str.trim())))
			return true;
		return false;
	}

	public static String getStringValueWithinQuotes(String str)
	{
		String value = str;
		
		if (value.startsWith("\""))
		{
			value = value.substring(1);
			if (!isNull(value))
			{
				if (value.indexOf("\"") != -1)
					value = value.substring(0, value.lastIndexOf("\""));
			}
		}
		return value;
	}
	
	
	public static Granularity getGranularityFromDateString(String dtString)
	{
		if (CNTROUtils.isNull(dtString))
			return Granularity.UNKNOWN;
		
		if ((dtString.indexOf(" AM ") != -1)||
			(dtString.indexOf(" PM ") != -1))
			return Granularity.MINUTE;
			
		if (dtString.toLowerCase().indexOf(" at ") != -1)
			return Granularity.HOUR;
		
		DateParser dp = new DateParser();
		Date parsedDate = dp.parse(dtString.trim(), new ParsePosition(0));
		
		if (parsedDate != null)
			return Event.getEventGranularityFromCalendarGranularity(dp.getDateGranularity());
		
		return Granularity.DAY;
	}
	
	public static Granularity getGranularityFromString(String str)
	{
		if (str.toLowerCase().indexOf("year") != -1)
			return Granularity.YEAR;

		if (str.toLowerCase().indexOf("month") != -1)
			return Granularity.MONTH;

		if (str.toLowerCase().indexOf("week") != -1)
			return Granularity.WEEK;

		if (str.toLowerCase().indexOf("day") != -1)
			return Granularity.DAY;

		return getGranularityFromDateString(str);
	}
	
	public static String removeGranularityInforamtionFromString(String str)
	{
		if (str == null)
			return null;
		
		String granularities[] = {"day", "days", "hour", "hours", "minute", "minutes", 
									"second", "seconds", "month", "months", "week", "weeks", 
									"year", "years", 
									"approximately", "approx", "approx.", "app",
									"estimated", "est", "est.", "about", "close", "close to",
									"exact", "exactly"};
		
		String auxiliaryWords[] = {"in", "a", "to", "the", "from"};
		
		String[] words = str.split("\\s+");
		for (int i = 0; i < words.length; i++) 
		{
			for (String g : granularities)
			{
				if (words[i].trim().toLowerCase().indexOf(g) != -1)
				{
					words[i] = "";
				}
			}
			
			if (CNTROUtils.isNull(words[i]))
				continue;
			
			for (String h : auxiliaryWords)
			{
				if ((words[i].trim().toLowerCase().indexOf(" " + h) != -1)||
						(words[i].trim().toLowerCase().indexOf("" + h + " ") != -1))
				{
					words[i] = "";
				}
			}
		}
		
		String convStr = "";
		for (int i = 0; i < words.length; i++) 
			convStr += words[i];
		
		return convStr;
	}
	
	public static long getNumericValueFromString(String pstr) throws Exception
	{
		String str = null;
		try
		{
			str = CNTROUtils.removeGranularityInforamtionFromString(pstr);
			
			Granularity[] gvals = Granularity.values();
			
			int pos = -1;
			
			str = str.trim();
			for (Granularity g : gvals)
				if (str.indexOf(g.toString()) != -1)
				{
					str = str.substring(0,  str.indexOf(g.toString()));
					break;
				}
			str = str.trim();
			
			// replace anything which is not alphbet or number
			str = str.replaceAll("[^a-zA-Z0-9.]", " ");
			
			try
			{
				return Long.parseLong(str.trim());
			}
			catch(Exception e)
			{
				return Words2Numbers.parse(str.trim());
			}
		}
		catch(Exception e)
		{
			// Now try splitting words and try them one by one
			try
			{
				if (str.trim().indexOf(" ") != -1)
				{
					String[] tokens = str.split(" ");
					long valueFromStringFragment = -1;
					
					for (String token :tokens)
					{
						valueFromStringFragment = CNTROUtils.getNumericValueFromString(token);
						if (valueFromStringFragment > -1)
							return valueFromStringFragment;
					}
				}
			}
			catch(Exception ae)
			{
			}
			
			if (str.trim().toLowerCase().indexOf("next") != -1)
				return 1;
			else
				if ((str.trim().toLowerCase().indexOf("prev") != -1)||
						(str.trim().toLowerCase().indexOf("previous") != -1))
					return -1;

			throw e;
		}
		
		//throw new Exception("Could not covert string \"" + str + "\" to its numeric value.");
	}
	
	public static CNTROException getException(String message)
	{
		return (new CNTROException());
	}
	
	public static void copyTimeInstantValuesIfNull(TimeInstant source, TimeInstant target)
	{
		if ((source == null)||(target == null))
			return;
		
		if (CNTROUtils.isNull(target.label)) target.label = source.label;
		if (CNTROUtils.isNull(target.getOriginalTime())) target.setOriginalTime(source.getOriginalTime());
		if ((target.granularity == null)||(target.granularity == Granularity.UNKNOWN)) target.granularity = source.granularity;
		if (target.getNormalizedTime() == null) target.setNormalizedTime(source.getNormalizedTime());
	}
	
	public static TemporalRelationType getInverseTemporalRelationType(
			TemporalRelationType temporalRelation) 
	{
		if (temporalRelation == null)
			return null;
		
		switch (temporalRelation) 
		{
			case BEFORE:  return TemporalRelationType.AFTER;
			case AFTER: return TemporalRelationType.BEFORE;
			case CONTAIN: return TemporalRelationType.DURING;
			case DURING: return TemporalRelationType.CONTAIN;
			
			/*
			case IS_INCLUDED: return TemporalRelationType.INCLUDE;
			case TERMINATE: return TemporalRelationType.INITIATE;
			case INCLUDE: return TemporalRelationType.IS_INCLUDED;
			case OVERLAPPED_BY: return TemporalRelationType.OVERLAP;
			case OVERLAP: return TemporalRelationType.OVERLAPPED_BY;
			case INITIATE: return TemporalRelationType.TERMINATE;
			case SAMEAS: return TemporalRelationType.SAMEAS;
			*/
			default:
				return temporalRelation;
		}
	}
	
	public static int getFinestGranularityBetweenEvents(Event event1, Event event2)
	{
		int g1 = event1.getTemporalGranularity();
		int g2 = event2.getTemporalGranularity();
		
		return ((g1 > g2)? g1 : g2); 
	}
	
	public static int getTemporalGranularityFromTime(Granularity granularity)
	{
		if (granularity == null)
			return Temporal.DAYS;
		
		switch(granularity)
		{
			case YEAR: return Temporal.YEARS;
			case MONTH: return Temporal.MONTHS;
			case HOUR: return Temporal.HOURS;
			case MINUTE: return Temporal.MINUTES;
			case SECOND: return Temporal.SECONDS;
			case WEEK:
			case DAY:
			case UNKNOWN:
			default: return Temporal.DAYS;
		}
	}

	public static TemporalRelationType findRelationBetween2Instants(Instant instant1, Instant instant2, int granularity) throws TemporalException
	{
		if ((instant1 == null)||(instant2 == null))
			return TemporalRelationType.UNKNOWN;
		
		if (instant1 == instant2)
			return TemporalRelationType.EQUAL;
		
		if (instant1.equals(instant2, granularity))
			return TemporalRelationType.EQUAL;
		
		if (instant1.after(instant2, granularity))
			return TemporalRelationType.AFTER;
		
		if (instant1.before(instant2, granularity))
			return TemporalRelationType.BEFORE;
		
		return TemporalRelationType.UNKNOWN;
	}

	public static TemporalRelationType findRelationBetween2Periods(Period period1, Period period2, int granularity) throws TemporalException
	{
		if ((period1 == null)||(period2 == null))
			return TemporalRelationType.UNKNOWN;
		
		if (period1 == period2)
			return TemporalRelationType.EQUAL;
		
		if (period1.equals(period2, granularity))
			return TemporalRelationType.EQUAL;
		
		if (period1.after(period2, granularity))
			return TemporalRelationType.AFTER;
		
		if (period1.before(period2, granularity))
			return TemporalRelationType.BEFORE;
		
		if (period1.contains(period2, granularity))
			return TemporalRelationType.INCLUDE;
		
		if (period2.contains(period2, granularity))
			return TemporalRelationType.IS_INCLUDED;
		
		if (period1.overlaps(period2, granularity))
			return TemporalRelationType.OVERLAP;
	
		if (period2.overlaps(period1, granularity))
			return TemporalRelationType.OVERLAPPED_BY;
	
		return TemporalRelationType.UNKNOWN;
	}
	
	public static Time getTimeFromCNTROCls(CNTROCls cntroCls)
	{
		if (cntroCls == null)
			return null;
		
		if (cntroCls instanceof TimeInstant)
			return (TimeInstant) cntroCls;

		if (cntroCls instanceof TimeInterval)
			return (TimeInterval) cntroCls;

		if (cntroCls instanceof TimePhase)
			return (TimePhase) cntroCls;

		if (cntroCls instanceof TimePeriod)
			return (TimePeriod) cntroCls;

		if (cntroCls instanceof Time)
			return (Time) cntroCls;
		
		return null;
	}
	
	public static void printIterator(Iterator<?> i, String header, String onlyWithThisSubstring, boolean skipEmptyOnes) 
	{
		if ((i != null)&&(!i.hasNext())&&(skipEmptyOnes))
			return;
		
        System.out.println(header);
        for(int c = 0; c < header.length(); c++)
            System.out.print("=");
        System.out.println();
        
        if(i.hasNext()) {
	        while (i.hasNext())
	        {
	        	Object o = i.next();
	        	if (o != null)
	        	{
	        		String toBePrinted = o.toString();
	        		
	        		if ((onlyWithThisSubstring != null)&&
	        			(toBePrinted.indexOf(onlyWithThisSubstring) == -1))
	        			continue;
	        		
	        		System.out.println(toBePrinted);
	        	}
	        }
        }       
        else
            System.out.println("<EMPTY>");
        
        System.out.println();
    }
	
	public static List<String> getTimeLineEventsDesc(Hashtable<String, List<Event>> timeline, 
													 boolean filterUnclassified, boolean useNormalizedEventTypes)
	{
		if (timeline == null)
			return null;
		
		List<String> str = new ArrayList<String>();
		
		Enumeration<String> keys = timeline.keys();
		List<String> listOfIndices = Collections.list(keys);
		Collections.sort(listOfIndices);
		String prev = "";
		String desc = "";
		String unclassifiedDesc = "";
		
		for (String key : listOfIndices)
		{
			if (filterUnclassified && (!CNTROModelConstants.isKeyForSequencedEvent(key)))
					continue;
				
			List<Event> le = timeline.get(key);
			
			if ((le == null)||(le.isEmpty()))
				continue;
			
			for (Event e : le)
			{
				if (useNormalizedEventTypes)
				{
					if (!prev.equals(e.normalizedEventCategory))
					{
						//if (!CNTROUtils.isNull(desc))
							//str.add("{" + desc + "}");
						prev = e.normalizedEventCategory;
						if (CNTROUtils.isNull(desc))
							desc = prev + " [" + e.description + "]";
						else
							desc += ": " + prev + " [" + e.description + "]";
					}
					else
					{
						if (CNTROUtils.isNull(desc))
							desc = prev + " [" + e.description + "]";
						else
							desc += ": [" + e.description + "]";
					}
				}
				else
				{
					if ("".equals(desc))
						desc += e.description;
					else
						desc += ": " + e.description;
				}
			}
			
			if (!CNTROModelConstants.isKeyForSequencedEvent(key))
			{
				unclassifiedDesc = desc;
				desc = "";
			}
			else
				if (!CNTROUtils.isNull(desc))
				{
					if (desc.trim().startsWith(":"))
						str.add(desc + "}");
					else
						str.add("{" + desc + "}");
				
					desc = "";
				}
			
		}
		
		if (!CNTROUtils.isNull(unclassifiedDesc))
			str.add("UNCLASSIFIED EVENTS:{" + unclassifiedDesc + "}");
		
		return str;
	}

	public static List<String> getTimeLineEventsDetails(Hashtable<String, List<Event>> timeline, 
			 boolean filterUnclassified)
	{
		if (timeline == null)
			return null;
		
		List<String> str = new ArrayList<String>();
		
		Enumeration<String> keys = timeline.keys();
		List<String> listOfIndices = Collections.list(keys);
		Collections.sort(listOfIndices);
		
		int seq = 0;
		for (String key : listOfIndices)
		{
			if (filterUnclassified && (!CNTROModelConstants.isKeyForSequencedEvent(key)))
				continue;
		
			List<Event> le = timeline.get(key);
		
			if ((le == null)||(le.isEmpty()))
				continue;
		    
			seq++;
			
			int nextIndentSeq = 1;
			List<Event> sortedSubListOfEvents = le;
			
			/*
			if (sortedSubListOfEvents.size() > 1)
			{
				Hashtable<String, List<Event>> subTimeLine = CNTROUtils.getEventsTimeline(false, false, false, false, le);
				
				if ((subTimeLine != null)&&(!subTimeLine.isEmpty())&&(subTimeLine.size() == sortedSubListOfEvents.size()))
				{
					List<String> sortedListOfEvents = CNTROUtils.getTimeLineEventsDetails(subTimeLine, false);
					str.addAll(sortedListOfEvents);
					return str;
				}
			}
			*/
			
			for (Event e : sortedSubListOfEvents)
			{
				TimeInstant et;
				try 
				{
					et = (TimeInstant) e.getTime(true);
				} 
				catch (CNTROException e1) 
				{
					System.out.println("Failed to get time for event:" + e.description);
					e1.printStackTrace();
					et = null;
				}
				
				String etStr = "Event Time could not be determined.";

				if (et != null)
				{
					String dateString = "" + et.getNormalizedTime();
					
					DateFormat formatter = new SimpleDateFormat("EEEE, dd MMMM yyyy, hh:mm:ss.SSS a");
					
					if (et.granularity == Granularity.YEAR)
						formatter = new SimpleDateFormat("yyyy");
					
					if (et.granularity == Granularity.MONTH)
						formatter = new SimpleDateFormat("MMM yyyy");
					
					if (et.granularity == Granularity.DAY)
						formatter = new SimpleDateFormat("MM/dd/yyyy");
					
					dateString = formatter.format(et.getNormalizedTime());
					
					etStr = "" + dateString + " , " + 
						   et.granularity + " , " +
						   et.assemblyMethod;
				}
				
				if (sortedSubListOfEvents.size() > 1)
					str.add("(" + seq + "." + (nextIndentSeq++) + ") " + e.normalizedEventCategory + " , " + e.description + " , " + etStr);
				else
					str.add("(" + seq + ") " + e.normalizedEventCategory + " , " + e.description + " , " + etStr);
			}
		}
		
		return str;
	}

	public static EventComparison compareTwoEvents(Event firstEvent, Event secondEvent) throws CNTROException 
	{
		Vector<TemporalRelationType> relations = new Vector<TemporalRelationType>();
		
		if ((firstEvent == null)||(secondEvent == null))
			throw new CNTROException("Cannot compare event is null");
		
		/*
		if (!firstEvent.hasAnyTemporalInformation())
			throw new CNTROException("Cannot compare event is null");
			
		if (!secondEvent.hasAnyTemporalInformation())
			throw new CNTROException("Cannot compare event is null");
		*/
		
		if (firstEvent.description.equals(secondEvent.description))
			return EventComparison.EQUAL;

		relations = new Vector<TemporalRelationType>();
		int granularity = CNTROUtils.getFinestGranularityBetweenEvents(firstEvent, secondEvent);
		Date start1 = firstEvent.findEventStartTime();
		Date start2 = secondEvent.findEventStartTime();
		Date end1 = firstEvent.findEventEndTime();
		Date end2 = secondEvent.findEventEndTime();

		JDBCDatetimeStringProcessor d = new JDBCDatetimeStringProcessor();
		Temporal temporal = new Temporal(d);

		try 
		{
			if ((firstEvent.isInstant())&&(secondEvent.isInstant())&&
				(start1 != null)&&(start2 != null))
			{
				Instant instant1 = new Instant(temporal, start1);
				Instant instant2 = new Instant(temporal, start2);
				relations.add(CNTROUtils.findRelationBetween2Instants(instant1, instant2, granularity));
			}
			else
			{
				if (((start1 != null)&&(start2 != null))&&(end1 != null)&&(end2 != null))
				{
					Period period1 = new Period(temporal, start1, end1);
					Period period2 = new Period(temporal, start2, end2);
					relations.add(CNTROUtils.findRelationBetween2Periods(period1, period2, granularity));
				}
			}
			
			EventComparison position = findRelativePositionOnTimeLine(relations);
			
			if (position != EventComparison.UNKNOWN)
				return position;
		} 
		catch (TemporalException e) 
		{
			e.printStackTrace();
		}

		try
		{
			TimeInstant t1 = (TimeInstant) firstEvent.getTime(true);
			TimeInstant t2 = (TimeInstant) secondEvent.getTime(true);
			
			if ((t1.assemblyMethod != TimeAssemblyMethod.ASSIGNED)&&
				(t2.assemblyMethod != TimeAssemblyMethod.ASSIGNED))
			{
				Date d1 = t1.getNormalizedTime();
				Date d2 = t2.getNormalizedTime();
				
				if ((d1 != null)&&(d2 != null))
				{
					Instant instant11 = new Instant(temporal, d1);
					Instant instant22 = new Instant(temporal, d2);
					relations.clear();
					relations.add(CNTROUtils.findRelationBetween2Instants(instant11, instant22, granularity));
				}
					
				EventComparison position = findRelativePositionOnTimeLine(relations);
				
				if (position != EventComparison.UNKNOWN)
					return position;
			}
			
		}
		catch(Exception e)
		{
			// DO Nothing
		}
		
		relations = firstEvent.getTemporalRelationTypeByRelation(secondEvent, true);
			
		return findRelativePositionOnTimeLine(relations);
	}
	
	public static EventComparison findRelativePositionOnTimeLine(Vector<TemporalRelationType> allrelations)
	{
		if ((allrelations != null)&&(!allrelations.isEmpty()))
		{
			if (allrelations.contains(TemporalRelationType.BEFORE)||
				allrelations.contains(TemporalRelationType.MEET)||
				allrelations.contains(TemporalRelationType.CONTAIN)||
				allrelations.contains(TemporalRelationType.OVERLAP))
				return EventComparison.BEFORE;
				
			if (allrelations.contains(TemporalRelationType.AFTER)||
				allrelations.contains(TemporalRelationType.DURING))
				// 
				// TODO: August 7, 2012: We are removing FINISH for now as we cannot conclude
				// that FINISH realtion will translate into AFTER 
				// for two given events. we need to change representation
				// of events using thier start and end timestamps.
				//
				//allrelations.contains(TemporalRelationType.FINISH))
				return EventComparison.AFTER;

			if (allrelations.contains(TemporalRelationType.SAMEAS)||
				allrelations.contains(TemporalRelationType.EQUAL)||
				allrelations.contains(TemporalRelationType.START)||
				allrelations.contains(TemporalRelationType.SIMULTANEOUS))
			{
				return EventComparison.EQUAL;
			}
		}
		
		return EventComparison.UNKNOWN;
	}
	
	public static Date getDateFromTime(Time time)
	{
		if (time == null)
			return null;
		
		if (time instanceof TimeInstant)
			return ((TimeInstant) time).getNormalizedTime();

		if (time instanceof TimeInterval)
		{
			Date rt = getDateFromTime(((TimeInterval) time).getStartTime());
			
			if (rt == null)
				rt = getDateFromTime(((TimeInterval) time).getEndTime());
			
			return rt;
		}

		if (time instanceof TimePhase)
		{
			Date rt = getDateFromTime(((TimePhase) time).getStartTime());
			
			if (rt == null)
				rt = getDateFromTime(((TimePhase) time).getEndTime());
			
			return rt;
		}
		
		return null;
	}
	
	public static Hashtable<String, List<Event>> getEventsTimelineOld(boolean reverseChronological, 
			 boolean filterUnclassified, 
			 boolean groupSameEvents,
			 boolean useNormalizedEventsIfAvailable,
			 List<Event> selectedEvents) 
	{
		List<Event> events = selectedEvents;
		Hashtable<String, List<Event>> returnList = new Hashtable<String, List<Event>>();
		
		if ((events == null)||(events.isEmpty()))
			return null;
		
		Collections.sort(events);
		
		if (reverseChronological)
			Collections.reverse(events);
		
		if (events.size() == 1)
		{
			returnList.put(CNTROModelConstants.getSeqEventKey(1), events);
			return returnList;
		}
		
		List<Event> unclassifieds = new ArrayList<Event>();
		Vector<String> processed = new Vector<String>();
		
		Hashtable<Integer, List<Event>> sortedEvents = new Hashtable<Integer, List<Event>>();
		
		for (int firstIndex = 0; firstIndex < events.size(); firstIndex++)
		{
			Event fe = events.get(firstIndex);
		
			if (processed.contains(fe.description))
				continue;
			else
				processed.add(fe.description);
		
			boolean unclassified = true;
			for (int secondIndex = 0; (secondIndex < events.size()); secondIndex++)
			{
				Event se = events.get(secondIndex);
				if (fe.description.equals(se.description))
					continue;
		
				try
				{
					switch(CNTROUtils.compareTwoEvents(fe, se))
					{
						case UNKNOWN : continue;
						default: unclassified = false;
					}
				}
				catch(Exception ex)
				{
					continue;
				}
		
				if (!unclassified)
					break;
			}
		
			if (unclassified)
			{
				unclassifieds.add(fe);
				continue;
			}
		
			Integer sameas = null;
			
			for (Integer seInd : sortedEvents.keySet())
			{
				List<Event> lst = sortedEvents.get(seInd);
		
				for (Event evt : lst)
				{
					try 
					{
						Vector<TemporalRelationType> allRelations = fe.getTemporalRelationTypeByRelation(evt, true);
						
						if (groupSameEvents)
							if (allRelations.contains(TemporalRelationType.EQUAL)||
								allRelations.contains(TemporalRelationType.SAMEAS)||
								allRelations.contains(TemporalRelationType.SIMULTANEOUS))
							{
								sameas = seInd;
								break;
							}
					}	 
					catch (CNTROException e)
					{
						e.printStackTrace();
					}
				}
		
				if (sameas != null)
					break;
			}
		
			if (sameas != null)
			{
				List<Event> syn = sortedEvents.get(sameas);
				syn.add(fe);
				sortedEvents.remove(sameas);
				sortedEvents.put(sameas, syn);
			}
			else
			{
				List<Event> thisEvent = new ArrayList<Event>();
				thisEvent.add(fe);
				sortedEvents.put(firstIndex, thisEvent);
			}
		}
		
		List<Integer> indices = Collections.list(sortedEvents.keys());
		Collections.sort(indices, new Comparator<Integer>()
		{
			public int compare(Integer o1, Integer o2) {
				return (o1.intValue() < o2.intValue() ? -1 : (o1.intValue() == o2.intValue() ? 0 : 1));
		}});
		
		for (Integer sortedIndex : indices)
		{
			List<Event> sl = sortedEvents.get(sortedIndex);
		
			if ((sl != null)&&(!sl.isEmpty()))
				returnList.put(CNTROModelConstants.getSeqEventKey(sortedIndex), sl);
		}
		
		if ((!filterUnclassified)&&(!unclassifieds.isEmpty()))
			returnList.put(CNTROModelConstants.getNotSeqEventKey(1), unclassifieds);
		
		return returnList;
	}
	
	public static Hashtable<String, List<Event>> getEventsTimeline(boolean reverseChronological, 
			 boolean filterUnclassified, 
			 boolean groupSameEvents,
			 boolean useNormalizedEventsIfAvailable,
			 Date assignThisTSIfNoneFound,
			 List<Event> selectedEvents) 
	{
		List<Event> events = selectedEvents;
		
		Hashtable<String, List<Event>> returnList = new Hashtable<String, List<Event>>();
		
		if ((events == null)||(events.isEmpty()))
			return null;
		
		if (assignThisTSIfNoneFound != null)
		{
			boolean isTSAsserted = isTimeStampAsserted(events);
			if (!isTSAsserted)
			{
				Collections.sort(events);
				Event firstEvent = events.get(0);
				TimeInstant assignTI = new TimeInstant("AssignedByUser", false, Granularity.DAY, "today - 10 Yrs", assignThisTSIfNoneFound, TimeAssemblyMethod.ASSERTED);
				
				firstEvent.eventTime = assignTI;
				
				List<TemporalRelationType> types = new ArrayList<TemporalRelationType>();
				types.add(TemporalRelationType.EQUAL);
				types.add(TemporalRelationType.SAMEAS);
				types.add(TemporalRelationType.SIMULTANEOUS);

				try
				{
					List<TemporalRelation> sameAsThisEvent = firstEvent.getTemporalRelationByRelationType(types);
					for (TemporalRelation se : sameAsThisEvent)
						if ((se.targetEvent != null)&&(!se.targetEvent.description.equals(firstEvent.description)))
							se.targetEvent.eventTime = assignTI;
				}
				catch(Exception e)
				{
					// DO Nothing
				}
				
				for (Event evt : events)
				{
					try 
					{
						evt.eventTime = evt.getTime(true);
					} 
					catch (CNTROException e) 
					{
						continue;
					}
				}
			}
		}
		
		Collections.sort(events);
		
		if (reverseChronological)
			Collections.reverse(events);
		
		if (events.size() == 1)
		{
			returnList.put(CNTROModelConstants.getSeqEventKey(1), events);
			return returnList;
		}
		
		List<Event> unclassifieds = new ArrayList<Event>();
		Vector<String> processed = new Vector<String>();
		
		LinkedList<List<Event>> sortedEvents = new LinkedList<List<Event>>();
		LinkedList<List<Event>> newSortedList = null;
		for (int firstIndex = 0; firstIndex < events.size(); firstIndex++)
		{
			Event fe = events.get(firstIndex);
		
			if (processed.contains(fe.description))
				continue;
			else
				processed.add(fe.description);
		
			boolean unclassified = true;
			for (int secondIndex = 0; (secondIndex < events.size()); secondIndex++)
			{
				Event se = events.get(secondIndex);
				if (fe.description.equals(se.description))
					continue;
		
				try
				{
					switch(CNTROUtils.compareTwoEvents(fe, se))
					{
						case UNKNOWN : continue;
						default: unclassified = false;
					}
				}
				catch(Exception ex)
				{
					continue;
				}
		
				if (!unclassified)
					break;
			}
		
			if (unclassified)
			{
				unclassifieds.add(fe);
				continue;
			}
		
			newSortedList = new LinkedList<List<Event>>();
			List<Event> thisEventA = new ArrayList<Event>();
			thisEventA.add(fe);
			newSortedList.add(thisEventA);
			//System.out.println("\n****  STARTING NEW LIST WITH " + fe.description + " *****\n");
			for (int i = 0; i < sortedEvents.size(); i++)
			{
				List<Event> lst = sortedEvents.get(i);
				
				for (Event evt : lst)
				{
					List<Event> sameAs = null;
					int afterIndex = -1;
					int beforeIndex = -1;
					for (List<Event> newevt : newSortedList)
					{
						for (Event nevt : newevt)
						{
							try 
							{
								//System.out.println("Comparing evt:" + evt.description + " with nevt:" + nevt.description);
								EventComparison comp = CNTROUtils.compareTwoEvents(evt, nevt);
								//System.out.println("Relation: " + comp);
								switch(comp)
								{
									case AFTER: 	afterIndex = newSortedList.indexOf(newevt);
													continue;
									case BEFORE : 	beforeIndex = newSortedList.indexOf(newevt);
													continue;
									case EQUAL: 	sameAs = newevt;
													break;
									default: 		beforeIndex = newSortedList.indexOf(newevt);
								}
							}	 
							catch (CNTROException e)
							{
								e.printStackTrace();
							}
						}
					}
					
					if (sameAs != null)
					{
						for (Event evtl : lst)
						{
							if (!sameAs.contains(evtl))
								sameAs.add(evtl);
						}
						continue;
					}
					
					if (afterIndex != -1)
					{
						try
						{
							newSortedList.add(afterIndex + 1, lst);
						}
						catch(Exception e)
						{
							//e.printStackTrace();
							newSortedList.addLast(lst);
						}
						break;
					}
					
					if (beforeIndex != -1)
					{
						try
						{
							newSortedList.add(beforeIndex - 1, lst);
						}
						catch(Exception e)
						{
							//e.printStackTrace();
							newSortedList.addFirst(lst);
						}
						break;
					}
				}
			}
			
			sortedEvents = newSortedList;
			newSortedList = null;
		}
		
		/*
		List<Integer> indices = Collections.list(sortedEvents.keys());
		Collections.sort(indices, new Comparator<Integer>()
		{
			public int compare(Integer o1, Integer o2) {
				return (o1.intValue() < o2.intValue() ? -1 : (o1.intValue() == o2.intValue() ? 0 : 1));
		}});

		for (Integer sortedIndex : indices)
		{
			List<Event> sl = sortedEvents.get(sortedIndex);
		
			if ((sl != null)&&(!sl.isEmpty()))
				returnList.put(CNTROModelConstants.getSeqEventKey(sortedIndex), sl);
		}

		*/
		
		int sIndex = 0;
		for (List<Event> sl : sortedEvents)
		{
			if ((sl != null)&&(!sl.isEmpty()))
				returnList.put(CNTROModelConstants.getSeqEventKey(sIndex++), sl);
		}

		if ((!filterUnclassified)&&(!unclassifieds.isEmpty()))
			returnList.put(CNTROModelConstants.getNotSeqEventKey(1), unclassifieds);
		
		return returnList;
	}
	
	public static boolean isTimeStampAsserted(List<Event> events)
	{
		for (Event evt : events)
		{
			try 
			{
				Time ti = evt.getTime(true);
				
				if (ti != null)
					return true;
			} 
			catch (CNTROException e) 
			{
				continue;
			}
			
		}
		return false;
	}
}
