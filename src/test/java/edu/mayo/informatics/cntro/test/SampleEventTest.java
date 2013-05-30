package edu.mayo.informatics.cntro.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.mayo.informatics.cntro.impl.CNTROQueryImpl;
import edu.mayo.informatics.cntro.main.CNTROAuxiliary;
import edu.mayo.informatics.cntro.model.Duration;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.model.Time;
import edu.mayo.informatics.cntro.model.timeline.CNTROTimeLineEventList;
import edu.mayo.informatics.cntro.queryIF.CNTROQuery;
import edu.mayo.informatics.cntro.queryIF.EventFeature;
import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.queryIF.ParserType;
import edu.mayo.informatics.cntro.utils.CNTROUtils;

public class SampleEventTest 
{
	private CNTROAuxiliary aux = null;
	private CNTROQuery query = null;
	
	@Before
	public void setUp() throws Exception 
	{
		System.out.println("Loading File:SampleEvents.owl");
		try
		{
			aux = new CNTROAuxiliary("src/test/resources/Sample/SampleEventsWithTimeStamps.owl");
			aux.parsertype = ParserType.OWLAPI; // default
			aux.loadOntology();
			aux.parse();
			query = new CNTROQueryImpl(aux);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void testTimeLine() 
	{
		System.out.println("######################## Testing TimeLine #####################################");
		System.out.println("Total Events=" + aux.parser_.getEventCount());
		assertTrue(aux.parser_.getEventCount() > 0);
		
		List<Event> eventList = query.findEvents("A 2.75X12MM TAXUS EXPRESS2 STENT WAS DEPLOYED IN THE RV BRANCH", false);
		assertFalse(eventList.isEmpty());
		assertTrue(eventList.size() == 1);
		
		List<Event> eventList2 = query.findEvents("PATIENT", false);
		assertFalse(eventList2.isEmpty());
		assertTrue(eventList2.size() > 1);

		try
		{
			Date time = query.getEventFeature(eventList.get(0), EventFeature.VALIDTIME, true);
			System.out.println("time = " + time);
			assertNotNull(time);
			
			Hashtable<String, List<Event>> events = query.getEventsTimeline(false, false, false, true, null);
			CNTROTimeLineEventList cetl = new CNTROTimeLineEventList();
			assertNotNull(events);
			// This is one way to process the event list. Hashtable key shows you the sequence on the timeline.
			String eventsStr = TestUtils.marshallTimeLine(events, false, cetl);
			
			//System.out.println("\n##################### Timeline Events in XML format ##########################");
			//System.out.println(eventsStr);
			//System.out.println("\n############################################################");
			
			List<String> tl = CNTROUtils.getTimeLineEventsDetails(events, false);
			System.out.println("\n##################### Timeline Sequence ##########################");
			System.out.println("\nTime line sequence is denoted by their serial number. \n" +
					"Integer part of sequence denotes the order. \n" +
					"Same items in same rank are seprated by decimal parts.");
			System.out.println("\n############################################################");
			for (String tle : tl)
				System.out.println("\nTime Line Sequence:" + tle);
			System.out.println("\n############################################################");
			
			assertNotNull(eventsStr);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testDuration() 
	{
		try
		{
			System.out.println("######################## Testing Duration #####################################");
			List<Event> eventList = query.findEvents("A 2.75X12MM TAXUS EXPRESS2 STENT WAS DEPLOYED IN THE RV BRANCH", false);
			assertFalse(eventList.isEmpty());
			assertTrue(eventList.size() == 1);
			
			Event startEvent = eventList.get(0);
			
			List<Event> eventList2 = query.findEvents("THE RCA AND LCX WERE FOUND TO BE OCCLUDED WITH THROMBUS", false);
			assertFalse(eventList2.isEmpty());
			assertTrue(eventList2.size() == 1);
			
			Event endEvent = eventList2.get(0);
			
			// Computing duration in months between 2006-2010
			Duration computed = query.getDurationBetweenEvents(startEvent, endEvent, Granularity.MONTH);
			assertTrue(computed.value == 48);
			System.out.println("Correct Duration found! = " + computed + " " + Granularity.MONTH + " between events:\n");
			System.out.println("Start Event" + startEvent.getClsId());
			System.out.println("Start Event Duration" + startEvent.getTime(true));
			System.out.println("\nEnd Event" + endEvent.getClsId());
			System.out.println("End Event Duration" + endEvent.getTime(true));
			System.out.println("\n############################################################");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
