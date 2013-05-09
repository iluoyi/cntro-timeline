package edu.mayo.informatics.cntro.test;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.XMLContext;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.model.CNTROModelConstants;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.model.Time;
import edu.mayo.informatics.cntro.model.TimeInstant;
import edu.mayo.informatics.cntro.model.timeline.AnnotationProperty;
import edu.mayo.informatics.cntro.model.timeline.CNTROTimeLineEvent;
import edu.mayo.informatics.cntro.model.timeline.CNTROTimeLineEventList;
import edu.mayo.informatics.cntro.model.timeline.CNTROTimeLineRelation;
import edu.mayo.informatics.cntro.model.timeline.CNTROTimeLineStatement;
import edu.mayo.informatics.cntro.model.timeline.types.TemporalRelation;
import edu.mayo.informatics.cntro.queryIF.CNTROQuery;
import edu.mayo.informatics.cntro.queryIF.EventFeature;
import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.queryIF.TemporalRelationType;
import edu.mayo.informatics.cntro.utils.CNTROUtils;

public class TestUtils 
{
	public static String marshallTimeLine(Hashtable<String, List<Event>> timeline, boolean filterUnclassified, CNTROTimeLineEventList cetl)
	{
		if (timeline == null)
			return null;
		
		List<String> str = new ArrayList<String>();
		
		Enumeration<String> keys = timeline.keys();
		List<String> listOfIndices = Collections.list(keys);
		Collections.sort(listOfIndices);
		
		Vector<String> allEvents = new Vector<String>();
		long currentEventIndex = 0;
		
		// defining a no evnet (does not exist) - just a filler.
		String noEventId = " ";
		CNTROTimeLineEvent noEvent = new CNTROTimeLineEvent();
		noEvent.setId(noEventId);
		noEvent.setName("ThisEventDoesNotExist");
		
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
			
			for (Event e : sortedSubListOfEvents)
			{
				if (!allEvents.contains(e.getClsId()))
					allEvents.add(e.getClsId());
				
				currentEventIndex = allEvents.indexOf(e.getClsId());
				
				CNTROTimeLineEvent cntroE = new CNTROTimeLineEvent();
				cntroE.setId("" +currentEventIndex);
				cntroE.setName(e.description);
				
				Time t = null;
				TimeInstant et = null;
				try 
				{
					t = e.getTime(true);
					
					if ((t != null) &&(t instanceof TimeInstant))
						et = (TimeInstant) t;
				} 
				catch (CNTROException e1) 
				{
					System.out.println("Failed to get time for event:" + e.description);
					e1.printStackTrace();
					et = null;
				}
				
				String etStr = "Could not compute time from annotations!";
				
				if (t != null)
				{
					String dateString = null;
				
					if (!(t instanceof TimeInstant))
					{
						dateString = "" + e.getEventFeature(EventFeature.STARTTIME);
						if (CNTROUtils.isNull(dateString))
							etStr = t.toString();
					}
					else
					{
						dateString = "" + ((TimeInstant) t).getNormalizedTime();
						
						etStr = "(Normalized Time= ";
						if (!CNTROUtils.isNull(dateString))
						{
							DateFormat formatter = new SimpleDateFormat("EEEE, dd MMMM yyyy, hh:mm:ss.SSS a");
						
							if (et.granularity == Granularity.YEAR)
								formatter = new SimpleDateFormat("yyyy");
						
							if (et.granularity == Granularity.MONTH)
								formatter = new SimpleDateFormat("MMM yyyy");
						
							if (et.granularity == Granularity.DAY)
								formatter = new SimpleDateFormat("MM/dd/yyyy");
						
							if (et.getNormalizedTime() != null)
								dateString = formatter.format(et.getNormalizedTime());
							else
								dateString = et.label;
						
							etStr += dateString;
						}
						
						etStr += ")";
						
						etStr +="(Granularity=" + t.granularity + ")(Computed As=" + t.assemblyMethod + ")";
					}
				}
				
				String thisStr = "";
				if (sortedSubListOfEvents.size() > 1)
					thisStr = "(" + seq + "." + (nextIndentSeq++) + ") " + e.normalizedEventCategory + " , " + e.description + " , " + etStr;
				else
					thisStr = "(" + seq + ") " + e.normalizedEventCategory + " , " + e.description + " , " + etStr;
				
				str.add(thisStr);
				
				AnnotationProperty eventAdditionalProperty1 = new AnnotationProperty();
				eventAdditionalProperty1.setName("description");
				eventAdditionalProperty1.setType("text");
				eventAdditionalProperty1.setContent(e.description);
				
				AnnotationProperty eventAdditionalProperty2 = new AnnotationProperty();
				eventAdditionalProperty2.setName("eventType");
				eventAdditionalProperty2.setType("text");
				eventAdditionalProperty2.setContent(e.normalizedEventCategory);

				AnnotationProperty eventAdditionalProperty3 = new AnnotationProperty();
				eventAdditionalProperty3.setName("time");
				eventAdditionalProperty3.setType("text");
				eventAdditionalProperty3.setContent(etStr);

				Vector<CNTROTimeLineStatement> cntroStmts = new Vector<CNTROTimeLineStatement>();
				
				for (TemporalRelation rel : TestCfg.rels)
				{
					
					CNTROTimeLineRelation tr = new CNTROTimeLineRelation();
					tr.setRelation(rel);
					tr.setTargetId(noEvent.getId());
					
					try
					{
						Vector<Event> targets = e.findTargetEventsForRelation(TestUtils.getCNTRORelType(rel));
						
						String tid = noEvent.getId();
						if (targets != null)
						{
							for (Event te : targets)
							{
								if (te == null)
									continue;
								
								if (!allEvents.contains(te.getClsId()))
									allEvents.add(te.getClsId());
								
								int tei = allEvents.indexOf(te.getClsId());
								
								if (CNTROUtils.isNull(tid))
									tid = "" + tei;
								else
								{
									boolean toAddIndex = true;
									if (tid.indexOf(TestCfg.ENTRY_DELIM) != -1)
									{
										String[] existingIds = tid.split(TestCfg.ENTRY_DELIM);
										
										for (String extId : existingIds)
										{
											try
											{
												int existingIntV = Integer.valueOf(extId.trim());
												if (existingIntV == tei)
												{
													toAddIndex = false;
													break;
												}
											}
											catch(Exception nfe) 
											{
											
											}
										}
									}
									else
									{
										try
										{
											int existingIntVal = Integer.valueOf(tid.trim());
											if (existingIntVal == tei)
												toAddIndex = false;
										}
										catch(Exception nfe) 
										{
											
										}
									}
									
									if (toAddIndex)
										tid += TestCfg.ENTRY_DELIM + tei;
								}
								// Create relation entry here...
								CNTROTimeLineStatement cstmt = new CNTROTimeLineStatement();
								cstmt.setSourceEvent(e.description);
								cstmt.setTargetEvent(te.description);
								cstmt.setRelation(rel);
								cntroStmts.add(cstmt);
							}
							
							tr.setTargetId(tid);	
						}
					}
					catch(Exception e2)
					{
						e2.printStackTrace();
					}
					
					cntroE.addRelatedTo(tr);
				}
				
				cntroE.addEventProperty(eventAdditionalProperty1);
				cntroE.addEventProperty(eventAdditionalProperty2);
				cntroE.addEventProperty(eventAdditionalProperty3);
				
				cetl.addEvent(cntroE);
				
				if (!cntroStmts.isEmpty())
					for (CNTROTimeLineStatement s : cntroStmts)
						cetl.addEventStmt(s);
			}
		}

		List<String> tldesc = CNTROUtils.getTimeLineEventsDesc(timeline, false, true);
		
		if (tldesc != null)
			for (String desc : tldesc)
				cetl.addTimelineEntry(desc);

		return TestUtils.getMarshalledText(cetl);
	}
	
	public static String getMarshalledText(CNTROTimeLineEventList eventList)
	{
		if (eventList == null)
			return null;
		
		try 
        {
			String xmlStr = null;

			XMLContext context = new XMLContext();

	        // Create a new Marshaller
	        Marshaller marshaller = context.createMarshaller();
	        
	        StringWriter sw = new StringWriter();
	        marshaller.setWriter(sw);
	        
	        // This does not indent the output of xml.
	        // work on it later to make sure the xml is pretty-print
	        marshaller.setProperty("org.exolab.castor.indent", "true");
	        
	        marshaller.marshal(eventList);
	        
	        return sw.toString();
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		}
		
        return null;
	}
	
	public static TemporalRelationType getCNTRORelType(TemporalRelation rel)
	{
		switch(rel)
		{
			case BEFORE : return TemporalRelationType.BEFORE;
			case DURING : return TemporalRelationType.DURING;
			case EQUAL : return TemporalRelationType.EQUAL;
			case FINISH : return TemporalRelationType.FINISH;
			case MEET : return TemporalRelationType.MEET;
			case OVERLAP : return TemporalRelationType.OVERLAP;
			case START : return TemporalRelationType.START;
		}
		
		return null;
	}
	
	public static InputStream getMarshalledInputStream(CNTROTimeLineEventList eventList)
	{
		if (eventList == null)
			return null;
		
		try 
        {
			String xmlStr = null;

			XMLContext context = new XMLContext();

	        // Create a new Marshaller
	        Marshaller marshaller = context.createMarshaller();
	        
	        StringWriter sw = new StringWriter();
	        marshaller.setWriter(sw);
	        
	        marshaller.marshal(eventList);
	        
	        InputStream is = new ByteArrayInputStream(sw.toString().getBytes("UTF-8"));
	        return is;
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		}
		
        return null;
	}
	
	public static Event getEvent(CNTROQuery query, String containtsText, boolean exactMatch)
	{
		List<Event> eventList = query.findEvents(containtsText, exactMatch);
		
		if ((eventList == null)||(eventList.isEmpty()))
			return null;
		
		return eventList.get(0);
	}
}
