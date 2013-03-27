package edu.mayo.informatics.cntro.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import edu.mayo.informatics.cntro.model.CNTROCls;
import edu.mayo.informatics.cntro.model.Event;

public class EventsHolder extends CNTROMap
{
	public Event getEventByLabel(String key)
	{
		CNTROCls cls = super.getByLabel(key);
		if ((cls != null)&&(cls instanceof Event))
			return (Event) cls;
		
		return null;
	}
	
	public List<Event> getEventByPhraseInLabel(String phrase)
	{
		if (CNTROUtils.isNull(phrase))
			return null;
		
		List<CNTROCls> clses = super.getClsByPhraseInLabel(phrase);
		if ((clses != null)&&(!clses.isEmpty()))
		{
			List<Event> events = new ArrayList<Event>();
			
			for (Iterator<CNTROCls> itr = clses.iterator(); itr.hasNext();)
			{
				CNTROCls o = itr.next();
				if ((o != null)&&(o instanceof Event))
					events.add((Event)o);
			}
			
			return events;
		}
		
		return null;
	}
	
	public List<Event> getAllEvents()
	{
		List<Event> evts = new ArrayList<Event>();
		Enumeration<CNTROCls> list = cntroClses.elements();
		
		while((list != null)&&(list.hasMoreElements()))
		{
			Object o = list.nextElement();
			if ((o != null)&&(o instanceof Event))
			evts.add((Event)o);
		}
		
		return evts;
  }
}
