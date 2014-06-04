package edu.mayo.informatics.cntro.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import edu.mayo.informatics.cntro.model.CNTROCls;

public class CNTROMap 
{
	protected Hashtable<String, CNTROCls> cntroClses = new Hashtable<String, CNTROCls>();
	
	public boolean add(CNTROCls cls)
	{
		if ((cls == null)||(CNTROUtils.isNull(cls.getClsId())))
			return false;
		
		this.cntroClses.put(cls.getClsId(), cls);
		return true;
	}
	
	// Yi: why do we use label as the key value?
	public CNTROCls getByLabel(String key)
	{
		if (CNTROUtils.isNull(key))
			return null;
		
		try
		{
			CNTROCls val = cntroClses.get(key);
			return val;
		}
		catch(Exception c)
		{
			System.out.println("Error while looking for an event with key=" + key);
		}
		
		return null;
	}
	
	public List<CNTROCls> getClsByPhraseInLabel(String phrase)
	{
		if (CNTROUtils.isNull(phrase))
			return null;
		
		List<CNTROCls> retList = new ArrayList<CNTROCls>();
		
		Enumeration<String> keys = cntroClses.keys();
		
		while (keys.hasMoreElements())
		{
			String key = keys.nextElement();
			
			if (CNTROUtils.isNull(key))
				continue;
			
			if (key.indexOf(phrase) != -1)
			{
				retList.add(cntroClses.get(key));
			}
		}
		
		return retList;
	}
	
	public void removeAll()
	{
		cntroClses = new Hashtable<String, CNTROCls>();
	}
	
	public void remove(CNTROCls cls)
	{
		if (cls == null)
			return;
		
		if (contains(cls))
			cntroClses.remove(cls.getClsId());
	}
	
	public List<CNTROCls> getAllClses()
	{
		CNTROCls[] evts = new CNTROCls[cntroClses.size()];
		int i = 0;
		for (Iterator<String> itr = cntroClses.keySet().iterator(); itr.hasNext();)
			evts[i++] = cntroClses.get(itr.next());
		
		return Arrays.asList(evts);
	}
	
	public int size()
	{
		return cntroClses.size();
	}
	
	public boolean isEmpty()
	{
		return cntroClses.isEmpty();
	}
	
	public String toString()
	{
		String ret = "";
		for (String ky : cntroClses.keySet())
		{
			ret += "\nKey=[" + ky + "]value=["  + ((cntroClses.get(ky) != null)?"NOT NULL": "NULL") + "]";
		}
		
		return ret;
	}
	
	public boolean contains(CNTROCls cls)
	{
		if (cls == null) return false;
		
		return (cntroClses.containsKey(cls.getClsId()));
	}
}
