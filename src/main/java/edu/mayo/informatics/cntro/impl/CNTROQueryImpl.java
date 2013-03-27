package edu.mayo.informatics.cntro.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.main.CNTROAuxiliary;
import edu.mayo.informatics.cntro.model.Duration;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.model.TemporalOffset;
import edu.mayo.informatics.cntro.model.TemporalRelation;
import edu.mayo.informatics.cntro.model.Time;
import edu.mayo.informatics.cntro.model.TimeInstant;
import edu.mayo.informatics.cntro.model.TimeInterval;
import edu.mayo.informatics.cntro.queryIF.CNTROQuery;
import edu.mayo.informatics.cntro.queryIF.EventFeature;
import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.queryIF.TemporalRelationType;
import edu.mayo.informatics.cntro.utils.CNTROUtils;
import edu.mayo.informatics.cntro.utils.DateParser;
import edu.mayo.informatics.cntro.utils.DateParserUtil;
import edu.mayo.informatics.cntro.utils.EventsHolder;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.Instant;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.JDBCDatetimeStringProcessor;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.Period;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.Temporal;
import edu.stanford.smi.protegex.owl.swrl.bridge.builtins.temporal.exceptions.TemporalException;

public class CNTROQueryImpl implements CNTROQuery 
{
	CNTROAuxiliary aux = null;
	public Temporal temporal = null;
	
	public CNTROQueryImpl(CNTROAuxiliary pAux)
	{
		aux = pAux;
		JDBCDatetimeStringProcessor d = new JDBCDatetimeStringProcessor();
		temporal = new Temporal(d);
	}

	public Duration getDuration(Event event) throws CNTROException 
	{
		if (event == null)
			return null;
		
		String msg = "";
		
		String label = "";
		
		long dur = -1;
		Granularity dg = event.getGranularity();
		
		try 
		{
			Date start = event.findEventStartTime();
			int granularity = event.getTemporalGranularity();
			
			Date end = event.findEventEndTime();
			
			if ((start != null)&&(end != null))
			{
				Period period = new Period(temporal, start, end);
				dur = period.duration(granularity);
			}
			
			if (event.isInstant())
			{
				if (start != null)
				{
					Instant instance = new Instant(temporal, start);
					dur = instance.duration(instance, granularity);
				}
				
				if (end != null)
				{
					Instant instance = new Instant(temporal, end);
					dur = instance.duration(instance, granularity);
				}
			}
		} 
		catch (TemporalException e) 
		{
			msg = e.getMessage();
		}
		
		if (dur == -1)
		{
			try
			{
				Time et = event.getTime(true);
				
				if (et != null)
				{
					if (et instanceof TimeInterval)
					{
						if (((TimeInterval)et).getDuration() != null)
							return ((TimeInterval)et).getDuration();
					}
					else
					{
						label = et.label;
						dg = CNTROUtils.getGranularityFromString(et.label);
						dur = CNTROUtils.getNumericValueFromString(et.label);
					}
				}
			}
			catch(Exception e2)
			{
				msg += e2.getMessage();
			}
		}
		
		if (dur > -1)
		{
			Duration d = new Duration();
			d.unit = dg;
			d.value = dur;
			d.label = label;
			
			return d;
		}
		
		throw new CNTROException(msg);
	}

	public long getDurationBetweenEvents(Event startEvent, Event endEvent)
					throws CNTROException 
	{
		return getDurationBetweenEvents(startEvent, endEvent, null);
	}

	public long getDurationBetweenEvents(Event startEvent, Event endEvent, Granularity granularity)
			throws CNTROException 
	{
		try 
		{
			if (startEvent == null)
				throw new CNTROException("Can't determine duration as start time is null.");

			if (endEvent == null)
				throw new CNTROException("Can't determine duration as start time is null.");

			Date endOf1 = startEvent.findEventEndTime();
			Date startOf2 = endEvent.findEventStartTime();
			
			if (endOf1 == null)
			{
				Time endTime = startEvent.getTime(true);
				
				if ((endTime != null)&&(endTime instanceof TimeInstant))
				{
					endOf1 = ((TimeInstant) endTime).getNormalizedTime();
					
					if (endOf1 == null)
					{
						if (!CNTROUtils.isNull(((TimeInstant) endTime).getOriginalTime()))
							endOf1 = DateParserUtil.parse(((TimeInstant) endTime).getOriginalTime());
						else
							endOf1 = DateParserUtil.parse(((TimeInstant) endTime).label);
					}
				}
			}

			if (startOf2 == null)
			{
				Time strTime = endEvent.getTime(true);
				
				if ((strTime != null)&&(strTime instanceof TimeInstant))
				{
					startOf2 = ((TimeInstant) strTime).getNormalizedTime();
					
					if (startOf2 == null)
					{
						DateParser dp = new DateParser();
						if (!CNTROUtils.isNull(((TimeInstant) strTime).getOriginalTime()))
							startOf2 = DateParserUtil.parse(((TimeInstant)strTime).getOriginalTime());
						else
							startOf2 = DateParserUtil.parse(((TimeInstant) strTime).label);
					}

				}
			}

			if ((endOf1 == null)||(startOf2 == null))
			{
				try
				{
					return getDurationFromOffset(startEvent, endEvent, granularity, false);
				}
				catch(Exception e)
				{
					return getDurationFromOffset(endEvent, startEvent, granularity, false);
				}
			}
			
			Period period = new Period(temporal, endOf1, startOf2);
			
			int temporalGranularity = CNTROUtils.getTemporalGranularityFromTime(Granularity.DAY);
			
			if (granularity == null)
				temporalGranularity = CNTROUtils.getFinestGranularityBetweenEvents(startEvent, endEvent);
			else
				temporalGranularity = CNTROUtils.getTemporalGranularityFromTime(granularity);
			
			return period.duration(temporalGranularity);
		} 
		catch (TemporalException e) 
		{
			e.printStackTrace();
			throw new CNTROException();
		}
	}

	public long convertValueBasedOnGranularity(Period period, Granularity fromGranularity, Granularity toGranularity) throws TemporalException
	{
		int defaultGranularity = CNTROUtils.getTemporalGranularityFromTime(Granularity.DAY);
		
		if ((fromGranularity == null)||(toGranularity == null))
			return period.duration(defaultGranularity);
		
		int toTempGranularity = CNTROUtils.getTemporalGranularityFromTime(toGranularity);
	    
		return period.duration(toTempGranularity);
	}

	private long getDurationFromOffset(Event startEvent, Event endEvent, Granularity granularity, boolean justUseSynonyms)
										throws CNTROException
	{
		long collectiveDur = 0;
		List<Event> eventstl = null;
		
		String msg = "Can't determine duration: Events are either not related (with or without offset)" +
				" or may have occurred during time narrower than granuality requested.";
		
		try
		{
			if (!justUseSynonyms)
				eventstl = eventsBetweenEvents(startEvent, endEvent, true, false, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new CNTROException(msg + "[" + e.getMessage() + "]");
		}
		
		if ((eventstl != null)&&(eventstl.size() >= 2)&&(!justUseSynonyms))
		{
			try
			{
				
				List<TemporalRelationType> types = new ArrayList<TemporalRelationType>();
				types.add(TemporalRelationType.EQUAL);
				types.add(TemporalRelationType.SAMEAS);
				types.add(TemporalRelationType.SIMULTANEOUS);

				boolean foundDuration = false;
				for (int a = 0; a < eventstl.size() -1; a++)
				{
					foundDuration = false;

					Event ae = eventstl.get(a);
					Event be = eventstl.get(a+1);
					try
					{
						if (ae.description.equals(be.description))
							continue;

						List<TemporalRelation> sameAsStartEvents = ae.getTemporalRelationByRelationType(types);
						List<TemporalRelation> sameAsEndEvents = be.getTemporalRelationByRelationType(types);
						
						if (sameAsStartEvents.isEmpty())
							sameAsStartEvents = be.getTemporalRelationByRelation(ae);

						if (sameAsEndEvents.isEmpty())
							sameAsEndEvents = ae.getTemporalRelationByRelation(be);
						
						if ((sameAsStartEvents.isEmpty())&&(sameAsEndEvents.isEmpty()))
								continue;
						
						Vector<Event> sameAsStartEventsList = new Vector<Event>();
						Vector<Event> sameAsEndEventsList = new Vector<Event>();
						
						sameAsStartEventsList.add(ae);
						sameAsEndEventsList.add(be);
						
						if (!sameAsStartEvents.isEmpty())
						{
							for (TemporalRelation relst : sameAsStartEvents)
							{
								boolean alreadythere1 = false;
								if (relst.targetEvent != null)
								{
									for (Event existing : sameAsStartEventsList)
										if (existing.getClsId().equals(relst.targetEvent.getClsId()))
											alreadythere1 = true;
								}
								
								if (!alreadythere1)
									sameAsStartEventsList.add(relst.targetEvent);
							}
						}
						
						if (!sameAsEndEvents.isEmpty())
						{
							for (TemporalRelation relen : sameAsEndEvents)
							{
								boolean alreadythere2 = false;
								if (relen.targetEvent != null)
								{
									for (Event existing : sameAsEndEventsList)
										if (existing.getClsId().equals(relen.targetEvent.getClsId()))
											alreadythere2 = true;
								}
								
								if (!alreadythere2)
									sameAsEndEventsList.add(relen.targetEvent);
							}
						}

						long countInThisCycle = 0;
						for (Event eventS : sameAsStartEventsList)
						{
							if (eventS == null)
								continue;

							if (foundDuration)
								break;
							
							for (Event eventE : sameAsEndEventsList)
							{
								if (foundDuration)
									break;
								
								if (eventE == null)
									continue;
								
								if (eventS.description.equals(eventE.description)) // if duplicate events
									continue;
								
								List<TemporalRelation> relo1 = eventS.getTemporalRelationByRelation(eventE);
								
								for (TemporalRelation relse : relo1)
								{
									if ((relse.relation == TemporalRelationType.SAMEAS)||(relse.relation == TemporalRelationType.EQUAL)||(relse.relation == TemporalRelationType.SIMULTANEOUS))
										continue;
									
									if (relse.offset != null)
									{
										System.out.println("\nDuration between(1st Check):" + relse.sourceEvent.description + " and " + relse.targetEvent.description);
										countInThisCycle = getDurationFromOffset(relse.sourceEvent, eventS, granularity, relse.offset);
										System.out.println("Duration:" + countInThisCycle);
										foundDuration = true;
										break;
									}
								}
									
								if (!foundDuration)
								{
									List<TemporalRelation> relo2 = eventE.getTemporalRelationByRelation(eventS);
									
									for (TemporalRelation reles : relo2)
									{
										if ((reles.relation == TemporalRelationType.SAMEAS)||(reles.relation == TemporalRelationType.EQUAL)||(reles.relation == TemporalRelationType.SIMULTANEOUS))
											continue;

										if (reles.offset != null)
										{
											System.out.println("\nDuration between(2nd Check):" + reles.sourceEvent.description + " and " + reles.targetEvent.description);
											countInThisCycle = getDurationFromOffset(reles.sourceEvent, eventE, granularity, reles.offset);
											System.out.println("Duration:" + countInThisCycle);
											foundDuration = true;
											break;
										}
									}
								}
							}
						}

						
						
						/*
						long countInThisCycle = 0;
						for (TemporalRelation rels : sameAsStartEvents)
						{
							if (foundDuration)
								break;

							for (TemporalRelation rele : sameAsEndEvents)
							{
								if (foundDuration)
									break;
								
								if (rels.targetEvent.description.equals(rele.targetEvent.description)) // if duplicate events
									continue;
								
								List<TemporalRelation> relo1 = rels.targetEvent.getTemporalRelationByRelation(rele.targetEvent);
								
								for (TemporalRelation relse : relo1)
								{
									//if ((relse.relation == TemporalRelationType.SAMEAS)||(relse.relation == TemporalRelationType.EQUAL)||(relse.relation == TemporalRelationType.SIMULTANEOUS))
										//duplicate = true;
										
									//if (duplicate) 
									//	continue;
									
									if (relse.offset != null)
									{
										System.out.println("\nDuration between(1st Check):" + relse.sourceEvent.description + " and " + relse.targetEvent.description);
										countInThisCycle = getDurationFromOffset(relse.sourceEvent, relse.targetEvent, granularity, relse.offset);
										System.out.println("Duration:" + countInThisCycle);
										foundDuration = true;
										break;
									}
								}
									
								if (!foundDuration)
								{
									List<TemporalRelation> relo2 = rele.targetEvent.getTemporalRelationByRelation(rels.targetEvent);
									
									for (TemporalRelation reles : relo2)
									{
										//if ((reles.relation == TemporalRelationType.SAMEAS)||(reles.relation == TemporalRelationType.EQUAL)||(reles.relation == TemporalRelationType.SIMULTANEOUS))
										//	duplicate = true;
										
										//if (duplicate) 
										//	continue;

										if (reles.offset != null)
										{
											System.out.println("\nDuration between(2nd Check):" + reles.sourceEvent.description + " and " + reles.targetEvent.description);
											countInThisCycle = getDurationFromOffset(reles.sourceEvent, reles.targetEvent, granularity, reles.offset);
											System.out.println("Duration:" + countInThisCycle);
											foundDuration = true;
											break;
										}
									}
								}
							}
						}
						*/
						
						if (foundDuration)
							collectiveDur += countInThisCycle;
					}
					catch(Exception e)
					{
						System.out.println("Failed while trying to find duration between " + ae.description + " and " + be.description + " Skipping...");
						System.out.println(e.getMessage());
					}
					
					if ((!foundDuration)&&(collectiveDur < 1))
						throw new CNTROException(msg);
				}
				return collectiveDur;
			}
			catch (Exception e)
			{
				throw new CNTROException(msg + "[" + e.getMessage() + "]");
			}
		}

		throw new CNTROException(msg);
	}
	
	
	private long getDurationFromOffset(Event startEvent, Event endEvent, Granularity granularity, TemporalOffset offset) 
	throws CNTROException
	{
		try
		{
			if (offset == null)
				System.out.println("offset is null");
			else
				System.out.println("Offset" + offset);
			
			Date sD = new Date();
			Calendar cal = Calendar.getInstance(Locale.US);
			cal.setTime(sD);
			int calendarUnit = Event.getCalendarGranularityFromEventGranularity(offset.unit);
			cal.add(calendarUnit, (int) (offset.value));
			Date eD =  cal.getTime();
		
			Period period = new Period(temporal, sD, eD);
		
			int temporalGranularity = CNTROUtils.getTemporalGranularityFromTime(Granularity.DAY);
			
			if (granularity == null)
			{
				temporalGranularity = CNTROUtils.getFinestGranularityBetweenEvents(startEvent, endEvent);
				return period.duration(temporalGranularity);
			}
			
			return convertValueBasedOnGranularity(period, offset.unit, granularity);
		}
		catch(TemporalException e)
		{
			e.printStackTrace();
			throw new CNTROException(e.getMessage());
		}
	}
	
	public Date getEventFeature(Event event, EventFeature feature, boolean computeIfNeeded)
			throws CNTROException 
	{
		if (event == null)
			return null;
		
		Date rd = event.getEventFeature(feature);
		
		if (rd == null)
			rd = CNTROUtils.getDateFromTime(event.getTime(computeIfNeeded));
		
		return rd;
	}

	public Vector<TemporalRelationType> getTemporalRelationType(Event oneEvent,
			Event twoEvent) throws CNTROException 
	{
		try 
		{
			Vector<TemporalRelationType> retRels = new Vector<TemporalRelationType>();
			
			if ((oneEvent == null)||(twoEvent == null))
				return null;
			
			Vector<TemporalRelationType> relations = oneEvent.getTemporalRelationTypeByRelation(twoEvent, true);
			
			if ((relations != null)&&(!relations.contains(TemporalRelationType.UNKNOWN)))
				return relations; // return first relation
			
			int granularity = CNTROUtils.getFinestGranularityBetweenEvents(oneEvent, twoEvent);
			Date start1 = oneEvent.findEventStartTime();
			Date start2 = twoEvent.findEventStartTime();
			Date end1 = oneEvent.findEventEndTime();
			Date end2 = twoEvent.findEventEndTime();

			if ((oneEvent.isInstant())&&(twoEvent.isInstant()))
			{
				Instant instant1 = new Instant(temporal, start1);
				Instant instant2 = new Instant(temporal, start2);
				{
					retRels.add(CNTROUtils.findRelationBetween2Instants(instant1, instant2, granularity));
					return retRels;
				}
			}
			
			if ((start1 == null)||
					(end1 == null)||
					(start2 == null)||
					(end2 == null))
			{
				retRels.add(TemporalRelationType.UNKNOWN);
				return retRels;
			}
			
			Period period1 = new Period(temporal, start1, end1);
			Period period2 = new Period(temporal, start2, end2);
			
			retRels.add(CNTROUtils.findRelationBetween2Periods(period1, period2, granularity));
			return retRels;
		} 
		catch (TemporalException e) 
		{
			e.printStackTrace();
			throw new CNTROException();
		}
	}

	public TemporalRelationType getTemporalRelationType(Event oneEvent,
			Time time) throws CNTROException 
	{
		if ((oneEvent == null)||(time == null))
			return null;
		
		Event temp = new Event(null);
		temp.eventTime = time;
		
		Vector<TemporalRelationType> rels = getTemporalRelationType(oneEvent, temp);
		
		if ((rels != null)&&(!rels.isEmpty()))
			return rels.elementAt(0);
		
		return null;
	}

	public List<Event> findEvents(String searchText) 
	{
		return findEvents(searchText, true);
	}
	
	public List<Event> findEvents(String searchText, boolean exactMatch) 
	{
		EventsHolder eventsHolder = (EventsHolder) aux.parser_.getEventMap();
		List<Event> evtList = new ArrayList<Event>();
		
		if (exactMatch)
		{
			Event evt = eventsHolder.getEventByLabel(searchText);
			if (evt != null)
				evtList.add(evt);
			return evtList;
		}
		
		evtList = eventsHolder.getEventByPhraseInLabel(searchText);
		
		return evtList;
	}

	public Hashtable<String, List<Event>> getEventsTimeline(boolean reverseChronological, 
															 boolean filterUnclassified, 
															 boolean groupSameEvents,
															 boolean useNormalizedEventsIfAvailable,
															 Date assignThisTSIfNoneFound) 
	{
		EventsHolder eventsHolder = (EventsHolder) aux.parser_.getEventMap();
		List<Event> events = eventsHolder.getAllEvents();

		return CNTROUtils.getEventsTimeline(reverseChronological, filterUnclassified, groupSameEvents, useNormalizedEventsIfAvailable, assignThisTSIfNoneFound, events);
	}
	

	/*
	public List<List<Event>> getEventsTimeline(boolean reverseChronological, boolean includeUnclassifiedEventsAtEnd) 
	{
		EventsHolder eventsHolder = (EventsHolder) aux.parser_.getEventMap();
		List<List<Event>> returnList = new ArrayList<List<Event>>();
		List<Event> events = eventsHolder.getAllEvents();
		
		if (events == null)
			return null;

		Collections.sort(events);
		
		if (reverseChronological)
			Collections.reverse(events);

		int firstIndex = 0;
		for (Event evt : events)
		{
			if (returnList.isEmpty())
			{
				List<Event> evtLst = new ArrayList<Event>();
				evtLst.add(evt);
				returnList.add(firstIndex, evtLst);
				continue;
			}
			
			List<Event> existing = returnList.get(firstIndex);
			
			boolean sameas = false;
			for (int secondIndex = 0; (secondIndex < existing.size())&&(!sameas); secondIndex++)
			{
				TemporalRelationType relation = TemporalRelationType.UNKNOWN;
				try 
				{
					relation = evt.getTemporalRelationTypeByRelation(existing.get(secondIndex));
				} catch (CNTROException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ((relation == TemporalRelationType.SAMEAS)||(relation == TemporalRelationType.SIMULTANEOUS))
				{
					existing.add(evt);
					sameas = true;
				}
			}
			
			if (!sameas)
			{
				List<Event> evtLst = new ArrayList<Event>();
				evtLst.add(evt);
				returnList.add(++firstIndex, evtLst);
			}
		}
		
		return returnList;
	}
	*/
	
	private List<Event> eventsBetweenEvents(Event startEvent, 
										   Event endEvent, 
										   boolean includeStartAndEndEvents,
										   boolean useNormalizedEvents,
										   Date assignThisTSIfNoneFound) 
										   throws CNTROException
	{
		
		boolean start = false;
		
		if (startEvent == null)
			start = true;
		
		boolean stop = false;
		
		Hashtable<String, List<Event>> timeline = getEventsTimeline(false, false, true, useNormalizedEvents, assignThisTSIfNoneFound);
		
		//System.out.println(CNTROUtils.getTimeLineEventsDesc(timeline, false));
		
		List<Event> section = new ArrayList<Event>();
		
		String startEvtStr = null;
		if (startEvent != null)
		{
			startEvtStr = startEvent.description;
		}
		String endEvtStr = null;
		
		if (endEvent != null)
		{
			endEvtStr = endEvent.description;
		}
		
		int j = 0;
		
		List<String> sortedEvents = CNTROUtils.getTimeLineEventsDesc(timeline, false, useNormalizedEvents);
		
		if (sortedEvents != null)
		{
			for (String desc : sortedEvents)
			{
				if (stop)
					continue;
			
				if ((endEvtStr != null)&&(desc.indexOf(endEvtStr) != -1))
				{
					stop = true;
					start = false;
					if ((includeStartAndEndEvents)&&(!endEvtStr.equals(startEvtStr)))
						section.add(j, endEvent);
				}
			
				if ((!start)&&(startEvtStr != null)&&(desc.indexOf(startEvtStr) != -1))
				{
					start = true;
					if (includeStartAndEndEvents)
						section.add(j++, startEvent);
					
					continue;
				}
			
				if (start)
				{
					List<Event> evts = findEvents(desc);
					
					if ((evts == null)||(evts.isEmpty()))
					{
						String events[] = null;
						if (desc.indexOf(",") != -1)
						{
							events = desc.split(",");
						}
						else
							events = new String[]{desc};
						
							if (events != null)
								for (int u=0; u < events.length; u++)
								{
									String evtName = events[u];
									
									if (evtName != null)
									{
										if (evtName.startsWith("{"))
											evtName = evtName.substring(1);

											if (evtName.endsWith("}"))
												evtName = evtName.substring(0, evtName.indexOf("}"));

										evts = findEvents(evtName);
									}
									
									if ((evts != null)&&(!evts.isEmpty()))
										break;
								}
						
					}
					
					if ((evts != null)&&(!evts.isEmpty()))
						section.add(j++, evts.get(0));
				}
			}
		}

		return section;
	}
}
