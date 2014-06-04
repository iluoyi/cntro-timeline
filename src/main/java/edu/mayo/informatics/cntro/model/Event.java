package edu.mayo.informatics.cntro.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.queryIF.EventFeature;
import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.queryIF.TemporalRelationType;
import edu.mayo.informatics.cntro.utils.CNTROUtils;


public class Event  extends CNTROCls implements Comparable<Event> 
{
	public boolean isComputed = false;
	public String description;
	public Time eventTime; // can be timeInstant, timeInterval, timePeriod, and timePhase...
	public Time noteTime; 
	private Vector<TemporalRelation> hasTemporalRelations = new Vector<TemporalRelation>();
	
	public String normalizedEventCategory = null; // TODO - Yi: what does this mean?
	
	public Event(String normalizedEventName)
	{
		super.setClsId(this.description);
		this.description = null;
		this.eventTime = null;
		this.noteTime = null;
		this.hasTemporalRelations = new Vector<TemporalRelation>();
		this.normalizedEventCategory = normalizedEventName;
	}
	
	public Event(String normalizedEventName, String desc, Time et, Time nt, Vector<TemporalRelation> vtr)
	{
		super.setClsId(desc);
		this.description = desc;
		this.eventTime = et;
		this.noteTime = nt;
		if (vtr == null)
			this.hasTemporalRelations = new Vector<TemporalRelation>();

		if (CNTROUtils.isNull(normalizedEventName))
			normalizedEventName = this.description;
		else
			this.normalizedEventCategory = normalizedEventName;
	}
	
	public String toString()
	{
		String rels = "";
		
		for (int i=0; i < this.hasTemporalRelations.size(); i++)
			rels += "\n---------------------\nTemporal Relation[" + (i+1) + "]=\n\t" + this.hasTemporalRelations.elementAt(i); 
		
		return "\n========================================\n{Event:" + this.getClsId() + "}" +
				"\n\t{Desc:" + this.description + "}" +
				"\n\t{EvtTime:" + this.eventTime + "}" + 
				"\n\t{NoteTime:" + this.noteTime + "}" +
				"\n\t{Relations: " + this.hasTemporalRelations.size() + " }\n" + 
			   		rels +
			   	"\n========================================\n";
	}

	public String toStringWithoutRelations()
	{
		return "\n========================================\n{Event:" + this.getClsId() + "}{Desc:" + this.description + 
			   "\n\t{EvtTime:" + this.eventTime + "}{" + 
			   "\n\t{NoteTime:" + this.noteTime + "}{" +
			   "\n\t{Relations: " + this.hasTemporalRelations.size() + " }" +
			   "\n========================================\n";
	}
	
	public Vector<TemporalRelationType> getTemporalRelationTypeByRelation(Event twoEvent, boolean checkRelatedEquals) throws CNTROException 
	{
		Vector<TemporalRelationType> allRelationBetween2Events = new Vector<TemporalRelationType>();
		
		try 
		{
			if (twoEvent == null)
				return null;
			 
			if ((twoEvent.hasTemporalRelations == null)&&
				(this.hasTemporalRelations == null))
			{
				allRelationBetween2Events.add(TemporalRelationType.UNKNOWN);
			}
			else
			{
				// Look into direct source to target relations.
				for (TemporalRelation relation : this.hasTemporalRelations)
				{
					if ((relation == null)||(relation.targetEvent == null))
						continue;

					String td = relation.targetEvent.description;
					
					if((!CNTROUtils.isNull(td))&&(td.equals(twoEvent.description)))
						allRelationBetween2Events.add(relation.relation);
				}
				
				// Did not find direct relation, look into target to source relations.
				if (allRelationBetween2Events.isEmpty())
				{
					for (TemporalRelation relation : twoEvent.hasTemporalRelations)
					{
						if ((relation == null)||(relation.targetEvent == null))
							continue;
						String td = relation.targetEvent.description;

						if((!CNTROUtils.isNull(td))&&(td.equals(this.description)))
							allRelationBetween2Events.add(CNTROUtils.getInverseTemporalRelationType(relation.relation));
					}
				}
				
				Vector<Event> firstEquals = this.findTargetEventsForRelation(TemporalRelationType.EQUAL);
				// Now if still no relation, see if into EQUAL Events to this event.
				if (allRelationBetween2Events.isEmpty())
					for (Event eqEv : firstEquals)
						allRelationBetween2Events.addAll(eqEv.getTemporalRelationTypeByRelation(twoEvent, false));
				
				Vector<Event> secondEquals = twoEvent.findTargetEventsForRelation(TemporalRelationType.EQUAL);
				// Now if still no relation, see if into EQUAL Events to second event.
				if (allRelationBetween2Events.isEmpty())
					for (Event eqEv : secondEquals)
						allRelationBetween2Events.addAll(eqEv.getTemporalRelationTypeByRelation(this, false));
				
				if ((allRelationBetween2Events.isEmpty())&&(checkRelatedEquals))
				{
					for (Event eqEv1 : firstEquals)
					{
						if (!allRelationBetween2Events.isEmpty()) 
							break;
						
						for (Event eqEv2 : secondEquals)
						{
							allRelationBetween2Events.addAll(eqEv1.getTemporalRelationTypeByRelation(eqEv2, false));
							if (!allRelationBetween2Events.isEmpty()) 
								break;
						}
					}
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new CNTROException();
		}
		
		return allRelationBetween2Events;
	}

	public Vector<Event> findTargetEventsForRelation(TemporalRelationType relationType) throws CNTROException 
	{
		Vector<Event> allTargetEventForGivenRelation = new Vector<Event>();
		
		try 
		{
			if (relationType != null)
			{
				Vector<TemporalRelation> relations = this.hasTemporalRelations;
			
				if (relations != null)
				{
					for (TemporalRelation tr : relations)
					{
						if ((tr.relation == relationType)&&(tr.targetEvent != null))
							allTargetEventForGivenRelation.add(tr.targetEvent);
					}
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new CNTROException();
		}
		
		return allTargetEventForGivenRelation;
	}

	public List<TemporalRelation> getTemporalRelationByRelation(Event twoEvent) throws CNTROException 
	{
		List<TemporalRelation> selectedRelations = new ArrayList<TemporalRelation>();
		
		try 
		{
			if (twoEvent == null)
				return selectedRelations;
			
			if ((twoEvent.hasTemporalRelations == null)||
				(this.hasTemporalRelations == null))
				return selectedRelations;
			
			Vector<TemporalRelation> relations = this.hasTemporalRelations;
			for (int i = 0; i < relations.size(); i++)
				if (relations.elementAt(i) != null)
					if (relations.elementAt(i).targetEvent != null)
						if (!CNTROUtils.isNull(relations.elementAt(i).targetEvent.description))
							if (relations.elementAt(i).targetEvent.description.equals(twoEvent.description))
								selectedRelations.add(relations.elementAt(i));
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new CNTROException();
		}
		
		return selectedRelations;
	}

	public List<TemporalRelation> getTemporalRelationByRelationType(List<TemporalRelationType> types) throws CNTROException 
	{
		List<TemporalRelation> selectedRelations = new ArrayList<TemporalRelation>();
		
		try 
		{
			if (this.hasTemporalRelations == null)
				return selectedRelations;
			
			Vector<TemporalRelation> relations = this.hasTemporalRelations;
			for (TemporalRelation rel : relations)
					for (TemporalRelationType type : types)
						if ((rel != null) && (rel.relation == type))
								selectedRelations.add(rel);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new CNTROException();
		}
		
		return selectedRelations;
	}

	public int compareTo(Event secondEvent) 
	{
		try
		{
			switch(CNTROUtils.compareTwoEvents(this, secondEvent))
			{
				case BEFORE : return -1;
				case AFTER : return 1;
				case EQUAL : return 0;
				default: return 1;
			}
		}
		catch (Exception e)
		{
			int max = 25;
			int max1 = (this.description.length() > max)? max : (this.description.length());
			int max2 = (secondEvent.description.length() > max)? max : (secondEvent.description.length());
			System.out.println("Events [ " + this.description.substring(0, max1) + " ...] and [ " + secondEvent.description.substring(0, max2) + " ...] are not related");
		}
		
		return 0;
	}


	public boolean isInstant()
	{
		if (this.eventTime != null)
			return (this.eventTime instanceof TimeInstant);
		
		if (this.noteTime != null)
			return (this.noteTime instanceof TimeInstant);
		
		return false;
	}
	
	public Date findEventStartTime()
	{
		Date et = null;
		et = getEventFeature(EventFeature.STARTTIME);
		
		if (et == null)
			et = getEventFeature(EventFeature.VALIDTIME);
		
		if (et == null)
			et = getEventFeature(EventFeature.NOTETIME);
		
		return et;
	}

	public Date findEventEndTime()
	{
		Date et = null;
		et = getEventFeature(EventFeature.ENDTIME);
		
		if (et == null)
			et = getEventFeature(EventFeature.VALIDTIME);
		
		if (et == null)
			et = getEventFeature(EventFeature.NOTETIME);
		
		return et;
	}
	
	public Date getEventFeature(EventFeature feature) 
	{
		Date rd = null;

		switch (feature) 
		{
			case ENDTIME:
				if ((this.eventTime != null)&&(this.eventTime instanceof TimeInterval))
				{
					Time tm = ((TimeInterval)this.eventTime).getEndTime();
					if ((tm != null)&&(tm instanceof TimeInstant))
						rd = ((TimeInstant)tm).getNormalizedTime();
				}
				break;
			case NOTETIME:
				if ((this.noteTime != null)&&(this.noteTime instanceof TimeInstant))
					rd = ((TimeInstant)this.noteTime).getNormalizedTime();
				break;
			case STARTTIME:
				if ((this.eventTime != null)&&(this.eventTime instanceof TimeInterval))
				{
					Time tm = ((TimeInterval)this.eventTime).getStartTime();
					if ((tm != null)&&(tm instanceof TimeInstant))
						rd = ((TimeInstant)tm).getNormalizedTime();
				}
				break;
			default:
				if ((this.eventTime != null)&&(this.eventTime instanceof TimeInstant))
					rd = ((TimeInstant)this.eventTime).getNormalizedTime();
				break;
		}
		return rd;
	}
	
	public int getTemporalGranularity()
	{
		if (this.eventTime != null)
			return CNTROUtils.getTemporalGranularityFromTime(this.eventTime.granularity);
		
		if (this.noteTime != null)
			return CNTROUtils.getTemporalGranularityFromTime(this.noteTime.granularity);
		
		return CNTROUtils.getTemporalGranularityFromTime(null);
	}

	public Granularity getGranularity()
	{
		if (this.eventTime != null)
			return this.eventTime.granularity;
		
		if (this.noteTime != null)
			return this.noteTime.granularity;
		
		return Granularity.DAY;
	}

	public boolean hasAnyTemporalInformation()
	{
		if (((this.hasTemporalRelations == null)||(this.hasTemporalRelations.isEmpty()))&&
				((findEventStartTime() == null)&&(findEventEndTime() == null)))
			return false;
		
		return true;
	}

	private Time getTime()
	{
		if (this.eventTime != null)
			return this.eventTime;
		
		return this.noteTime;
	}

	public Time getTime(boolean computeIfNeeded) throws CNTROException
	{
		if (this.getTime() == null)
		{
			if (computeIfNeeded)
			{
				Time rt = this.computeEventTimeUsingTemporalRelations();
				
				if (rt == null)
				{
					List<TemporalRelationType> types = new ArrayList<TemporalRelationType>();
					types.add(TemporalRelationType.EQUAL);
					types.add(TemporalRelationType.SAMEAS);
					types.add(TemporalRelationType.SIMULTANEOUS);
	
					List<TemporalRelation> sameEvents = this.getTemporalRelationByRelationType(types);
					for (TemporalRelation se : sameEvents)
					{
						if (se.targetTime != null)
							rt = se.targetTime;
						else
						{
							if (se.targetEvent != null)
								rt = se.targetEvent.computeEventTimeUsingTemporalRelations();
						}
						
						if (rt == null)
							continue;
						else
							break;
					}
				}
				
				return rt; 
			}
		}
		
		return this.getTime();
	}
	
	public Vector<TemporalRelation> getTemporalRelations()
	{
		return this.hasTemporalRelations;
	}
	
	// Yi: to add a temporal relation (a common one or an offset)
	public void addTemporalRelation(TemporalRelation rel)
	{
		boolean found = false;
		int index = -1;
		
		if (rel == null)
			return;
		
		String srcE = null;
		if (rel.sourceEvent == null)
			return;
		else
			srcE = rel.sourceEvent.description;
		
		String trgE = null;
		if (rel.targetEvent == null)
			return;
		else
			trgE = rel.targetEvent.description;
		
		if ((srcE != null)&&(trgE != null)&&(srcE.equals(trgE)))
			return;
		
		for (int i=0; i < this.hasTemporalRelations.size(); i++)
			if (this.hasTemporalRelations.elementAt(i).getClsId().equals(rel.getClsId()))
			{
				found = true;
				index = i;
			}
		
		if ((this.hasTemporalRelations.isEmpty())||(!found))
			this.hasTemporalRelations.add(rel);
		
		if ((found)&&(index != -1))
		{
			this.hasTemporalRelations.remove(index);
			this.hasTemporalRelations.add(rel);
		}
	}
	
	private Time computeEventTimeUsingTemporalRelations() throws CNTROException
	{
		for (TemporalRelation rel : getTemporalRelations())
		{
			Time ti = computeTime(rel);
			
			if (ti != null)
				return ti;
		}
		
		return null;
	}
	/*
	private Time computeEventTimeUsingTemporalRelations() throws CNTROException
	{
		TimeInstant prev = null;
		TimeInstant curr = null;
		Time ti = null;
		for (TemporalRelation rel : getTemporalRelations())
		{
			ti = computeTime(rel);
			
			
			if (ti instanceof TimeInstant)
				curr = (TimeInstant) ti;
			
			if ((curr != null)&&(prev != null))
			{
				JDBCDatetimeStringProcessor d = new JDBCDatetimeStringProcessor();
				Temporal temporal = new Temporal(d);

				Date d1 = prev.normalizedTime;
				Date d2 = curr.normalizedTime;
				if ((d1 != null)&&(d2 != null))
				{
					TemporalRelationType tr = null;
					try 
					{
						Instant instant11 = new Instant(temporal, d1);
						Instant instant22 = new Instant(temporal, d2);
						
						tr = CNTROUtils.findRelationBetween2Instants(instant11, instant22, 
												CNTROUtils.getTemporalGranularityFromTime(curr.granularity));
					} catch (TemporalException e) 
					{
						e.printStackTrace();
					}
					
					if (tr == TemporalRelationType.BEFORE)
						prev = curr;
				}
			}
			
			if (prev == null)
				prev = curr;
		}
		
		if (prev != null)
			return prev;
		
		return ti;
	}
	
	*/
	
	private Time computeTime(TemporalRelation rel) throws CNTROException
	{
		if (rel == null)
			return null;
		
		if (rel.targetEvent != null)
		{
			if (rel.targetEvent.getTime() != null)
			{
				if (this.equals(rel.targetEvent))
					return rel.targetEvent.getTime();
				else
				{
					if (rel.relation == TemporalRelationType.BEFORE)
					{
						Date sp = null;
						Time tti = rel.targetEvent.getTime();
						
						if (tti instanceof TimeInstant)
						{
							sp = ((TimeInstant) tti).getNormalizedTime();
						}
						
						if (tti instanceof TimeInterval)
						{
							sp = rel.targetEvent.findEventStartTime();
						}

						if ((sp != null)&&(rel.offset != null))
						{
							TimeAssemblyMethod method = TimeAssemblyMethod.INFERRED;
							Granularity unit = null;
							Granularity targetUnit = rel.targetEvent.getGranularity();
							long value = 0;
							
							unit = rel.offset.unit;
							value = rel.offset.value;
							
							/*if (rel.offset != null)
							{
								unit = rel.offset.unit;
								value = rel.offset.value;
							}
							else
							{
								// we are assigning value so we go one granularity down and then add just one unit of that so that we
								// can still maintain the relation with assigned value
								unit = getNextGranularity(targetUnit, false);
								value = 1;
								method = TimeAssemblyMethod.ASSIGNED;
							}*/

							//targetUnit = getNextGranularity(targetUnit, true);
							
							Calendar cal = Calendar.getInstance(Locale.US);
							cal.setTime(sp);
							int calendarUnit = getCalendarGranularityFromEventGranularity(unit);
							cal.add(calendarUnit, (int) (value * (-1)));
							Date retDt =  cal.getTime();
							TimeInstant retTi = new TimeInstant("Computed", 
													false, 
													targetUnit, 
													retDt.toString(), 
													retDt, 
													method);
							return retTi;
							
						}
					}
					
					if (rel.relation == TemporalRelationType.AFTER)
					{
						Date sp = null;
						Time tti = rel.targetEvent.getTime();
						
						if (tti instanceof TimeInstant)
						{
							sp = ((TimeInstant) tti).getNormalizedTime();
						}
						
						if (tti instanceof TimeInterval)
						{
							sp = rel.targetEvent.findEventEndTime();
						}

						if ((sp != null)&&(rel.offset != null))
						{
							TimeAssemblyMethod method = TimeAssemblyMethod.INFERRED;
							Granularity targetUnit = rel.targetEvent.getGranularity();
							Granularity unit = null;
							long value = 0;
							
							unit = rel.offset.unit;
							value = rel.offset.value;
							
							/*if (rel.offset != null)
							{
								unit = rel.offset.unit;
								value = rel.offset.value;
							}
							else
							{
								// we are assigning value so we go one granularity down and then add just one unit of that so that we
								// can still maintain the relation with assigned value
								unit = getNextGranularity(targetUnit, false);
								value = 1;
								method = TimeAssemblyMethod.ASSIGNED;
							}*/

							//targetUnit = getNextGranularity(targetUnit, true);
							
							Calendar cal = Calendar.getInstance(Locale.US);
							cal.setTime(sp);
							int calendarUnit = getCalendarGranularityFromEventGranularity(unit);
							cal.add(calendarUnit, (int) (value));
							Date retDt =  cal.getTime();
							TimeInstant retTi = new TimeInstant("Computed", 
													false, 
													targetUnit, 
													retDt.toString(), 
													retDt, 
													method);
							return retTi;
							
						}
					}
				}
			}
		}
		else
		{
			if (rel.targetTime != null)
			{
				if ((rel.relation == TemporalRelationType.SAMEAS)||(rel.relation == TemporalRelationType.SIMULTANEOUS)||(rel.relation == TemporalRelationType.EQUAL))
					return rel.targetTime;
				else
				{
					if (rel.relation == TemporalRelationType.BEFORE)
					{
						Date sp = null;
						Time tti = rel.targetTime;
						
						if (tti instanceof TimeInstant)
						{
							sp = ((TimeInstant) tti).getNormalizedTime();
						}
						
						if (tti instanceof TimeInterval)
						{
							sp = rel.targetEvent.findEventStartTime();
						}

						if ((sp != null)&&(rel.offset != null))
						{
							TimeAssemblyMethod method = TimeAssemblyMethod.INFERRED;
							Granularity targetUnit = rel.targetEvent.getGranularity();
							Granularity unit = null;
							long value = 0;
							
							unit = rel.offset.unit;
							value = rel.offset.value;
							
							/*if (rel.offset != null)
							{
								unit = rel.offset.unit;
								value = rel.offset.value;
							}
							else
							{
								// we are assigning value so we go one granularity down and then add just one unit of that so that we
								// can still maintain the relation with assigned value
								unit = getNextGranularity(targetUnit, false);
								value = 1;
								method = TimeAssemblyMethod.ASSIGNED;
							}*/

							//targetUnit = getNextGranularity(targetUnit, true);
							
							Calendar cal = Calendar.getInstance(Locale.US);
							cal.setTime(sp);
							int calendarUnit = Event.getCalendarGranularityFromEventGranularity(unit);
							cal.add(calendarUnit, (int) (value * (-1)));
							Date retDt =  cal.getTime();
							TimeInstant retTi = new TimeInstant("Computed", 
													false, 
													targetUnit, 
													retDt.toString(), 
													retDt, 
													method);
							return retTi;
							
						}
					}
					
					if (rel.relation == TemporalRelationType.AFTER)
					{
						Date sp = null;
						Time tti = rel.targetTime;
						
						if (tti instanceof TimeInstant)
						{
							sp = ((TimeInstant) tti).getNormalizedTime();
						}
						
						if (tti instanceof TimeInterval)
						{
							sp = rel.targetEvent.findEventEndTime();
						}

						if ((sp != null)&&(rel.offset != null))
						{
							TimeAssemblyMethod method = TimeAssemblyMethod.INFERRED;
							Granularity targetUnit = rel.targetEvent.getGranularity();
							Granularity unit = null;
							long value = 0;
							
							unit = rel.offset.unit;
							value = rel.offset.value;
							
							/*if (rel.offset != null)
							{
								unit = rel.offset.unit;
								value = rel.offset.value;
							}
							else
							{
								// we are assigning value so we go one granularity down and then add just one unit of that so that we
								// can still maintain the relation with assigned value
								unit = getNextGranularity(targetUnit, false);
								value = 1;
								method = TimeAssemblyMethod.ASSIGNED;
							}*/

							//targetUnit = getNextGranularity(targetUnit, true);
							
							Calendar cal = Calendar.getInstance(Locale.US);
							cal.setTime(sp);
							int calendarUnit = Event.getCalendarGranularityFromEventGranularity(unit);
							cal.add(calendarUnit, (int) (value * (-1)));
							Date retDt =  cal.getTime();
							TimeInstant retTi = new TimeInstant("Computed", 
													false, 
													targetUnit, 
													retDt.toString(), 
													retDt, 
													method);
							return retTi;
							
						}
					}
				}
			}
		}
		
		return null;
	}
	
	public static int getCalendarGranularityFromEventGranularity(Granularity gran)
	{
		switch (gran)
		{
			case HOUR: return Calendar.HOUR;
			case MINUTE: return Calendar.MINUTE;
			case MILLISECOND: return Calendar.MILLISECOND;
			case MONTH: return Calendar.MONTH;
			case SECOND: return Calendar.SECOND;
			case WEEK: return Calendar.WEEK_OF_YEAR;
			case YEAR: return Calendar.YEAR;
			default: return Calendar.DAY_OF_YEAR;
		}
	}
	
	public static int compareGranularities(Granularity g1, Granularity g2)
	{
		int g1int = getCalendarGranularityFromEventGranularity(g1);
		int g2int = getCalendarGranularityFromEventGranularity(g2);
		
		return ((g1int == g2int)? 0 : ((g1int > g2int)? 1: -1));
	}
	
	public static Granularity getNextGranularity (Granularity starting, boolean up)
	{
		if ((!up)&&(starting == Granularity.MILLISECOND))
			return Granularity.MILLISECOND;
		
		if ((up)&& (starting == Granularity.YEAR))
			return Granularity.YEAR;
		
		switch (starting)
		{
			case HOUR: return ((up)? Granularity.DAY : Granularity.MINUTE);
			case MINUTE: return ((up)? Granularity.HOUR : Granularity.SECOND);
			case MILLISECOND: return ((up)? Granularity.SECOND : Granularity.MILLISECOND);
			case MONTH: return ((up)? Granularity.YEAR : Granularity.WEEK);
			case SECOND: return ((up)? Granularity.MINUTE : Granularity.MILLISECOND);
			case WEEK: return ((up)? Granularity.MONTH : Granularity.DAY);
			case YEAR: return ((up)? Granularity.YEAR : Granularity.MONTH);
			case DAY: return ((up)? Granularity.WEEK : Granularity.HOUR);
			default: return Granularity.DAY;
		}
	}
	
	public static Granularity getEventGranularityFromCalendarGranularity(int gran)
	{
		switch (gran)
		{
			case Calendar.HOUR: return Granularity.HOUR;
			case Calendar.MINUTE: return Granularity.MINUTE;
			case Calendar.MILLISECOND: return Granularity.MILLISECOND;
			case Calendar.MONTH: return Granularity.MONTH;
			case Calendar.SECOND: return Granularity.SECOND;
			case Calendar.WEEK_OF_YEAR: return Granularity.WEEK;
			case Calendar.YEAR: return Granularity.YEAR;
			default: return Granularity.DAY;
		}
	}

}
