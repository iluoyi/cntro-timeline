package edu.mayo.informatics.cntro.utils;

import edu.mayo.informatics.cntro.queryIF.TemporalRelationType;

public class RelationId 
{
	public static final String CNTRO_RELATION_ID_DELIM = "@@&@&@&@@";
	public static String createRelationId(String srcId, String trgId, TemporalRelationType type)
	{
		return "" + srcId + 
		CNTRO_RELATION_ID_DELIM + 
		type + 
		CNTRO_RELATION_ID_DELIM + 
		trgId;
	}
	
	public static String getRelationSource(String key)
	{
		String[] parts = getParts(key);
		if (parts != null)
			return parts[0];
		return null;
	}
	
	public static String getRelationTarget(String key)
	{
		String[] parts = getParts(key);
		if (parts != null)
			return parts[2];
		return null;
	}
	
	public static String getRelationType(String key)
	{
		String[] parts = getParts(key);
		if (parts != null)
			return parts[4];
		return null;
	}
	
	public static String[] getParts(String key)
	{
		if (CNTROUtils.isNull(key))
			return null;
		
		String[] parts = key.split(CNTRO_RELATION_ID_DELIM);
		if (parts.length != 5)
			return null;
		
		return parts;
	}
}
