package edu.mayo.informatics.cntro.model;

import edu.mayo.informatics.cntro.queryIF.Granularity;

public class Duration extends CNTROCls
{
	public boolean isAsserted = false;
	public String label = null;
	public long value;
	public Granularity unit = Granularity.UNKNOWN;
	
	public Duration()
	{
		this.label = null;
		this.value = 0;
		this.unit = Granularity.UNKNOWN;
	}
	
	public Duration(String lbl, int val, Granularity gran)
	{
		this.label = lbl;
		this.value = val;
		this.unit = gran;
	}
	
	public String toString()
	{
		return ""  + ((this.label != null)? ("{Dur. label:" + this.label + "}"):"") +
						((this.unit != null)? ("{Dur. unit:" + this.unit + "}"):"") +
						"{Dur. value:" + this.value + "}" ;
	}
	
	public String getClsId()
	{
		return ""  + ((this.label != null)? (this.label):"") +
		((this.unit != null)? ("this.unit}"):"") +
		this.value ;
	}
}
