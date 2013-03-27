package edu.mayo.informatics.cntro.queryIF;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.model.Duration;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.model.Time;

public interface CNTROQuery 
{
	public List<Event> findEvents(String searchText, boolean exactMatch);
	public Date getEventFeature(Event event, EventFeature feature, boolean computeIfNeeded) throws CNTROException;
	public Duration getDuration(Event event) throws CNTROException;
	public long getDurationBetweenEvents(Event startEvent, Event endEvent, Granularity granularity) throws CNTROException;
	public Vector<TemporalRelationType> getTemporalRelationType(Event oneEvent, Event twoEvent)  throws CNTROException;
	public TemporalRelationType getTemporalRelationType(Event oneEvent, Time time)  throws CNTROException;
	public Hashtable<String, List<Event>> getEventsTimeline(boolean reverseChronological, boolean filterUnclassified, boolean groupSameEvents, boolean useNormalizedEventsIfAvailable, Date assignThisTSIfNoneFound);
}
