package edu.mayo.informatics.cntro.serviceIF;

import java.util.Collection;

import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.utils.CNTROMap;

public interface CNTROParser 
{
	// return value tells you if parsing was 
	// error free or not. if true, it went through without errors
	// if false, there were some errors.
	public boolean parse();
	public Collection<Event> getEvents();
	public int getEventCount();
	public CNTROMap getEventMap();
	public CNTROMap getRelationMap();
}
