package edu.mayo.informatics.cntro.model;

public class CNTROModelConstants 
{
	// A Event has enough temporal information to be on time-line. 
	public static final String EVENT_SEQUENCED_PREFIX = "SEQ-";
	
	// A Event does not have enough temporal information to be on time-line.
	public static final String EVENT_NOT_SEQUENCED_PREFIX = "NOT-SEQ-";
	
	public static String getSeqEventKey(int index)
	{
		return EVENT_SEQUENCED_PREFIX + index;
	}
	
	public static String getNotSeqEventKey(int index)
	{
		return EVENT_NOT_SEQUENCED_PREFIX + index;
	}
	
	public static boolean isKeyForSequencedEvent(String key)
	{
		if (key == null)
			return false;
		
		return (key.startsWith(EVENT_SEQUENCED_PREFIX));
	}
}
