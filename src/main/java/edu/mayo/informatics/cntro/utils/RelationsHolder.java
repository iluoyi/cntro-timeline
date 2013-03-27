package edu.mayo.informatics.cntro.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.mayo.informatics.cntro.model.CNTROCls;
import edu.mayo.informatics.cntro.model.TemporalRelation;

public class RelationsHolder extends CNTROMap
{
	public TemporalRelation getRelationByLabel(String key)
	{
		CNTROCls cls = super.getByLabel(key);
		if ((cls != null)&&(cls instanceof TemporalRelation))
			return (TemporalRelation) cls;
		
		return null;
	}
	
	public List<TemporalRelation> getAllRelations()
	{
		TemporalRelation[] rels = new TemporalRelation[size()];
		int i = 0;
		for (Iterator<String> itr = cntroClses.keySet().iterator(); itr.hasNext();)
		{
			Object o = itr.next();
			Object r = cntroClses.get(o);
			if ((r != null)&&(r instanceof TemporalRelation))
			rels[i++] =(TemporalRelation)r;
		}
		return Arrays.asList(rels);
	}
	
	public Set<String> getAllRelationIds()
	{
		return cntroClses.keySet();
	}
}
