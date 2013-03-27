package edu.mayo.informatics.cntro.model;

import edu.mayo.informatics.cntro.queryIF.Granularity;

public class Time extends CNTROCls
{
	public TimeAssemblyMethod assemblyMethod = TimeAssemblyMethod.UNKNOWN;
	public boolean isAdjusted = false;
	public String label = null;
	public boolean modality = false;
	public Granularity granularity = Granularity.UNKNOWN;
	
	public Time(TimeAssemblyMethod method)
	{
		if (method != null)
			this.assemblyMethod = method;
	}

	public Time(String label, boolean modality, Granularity granularity, TimeAssemblyMethod method) 
	{
		super.setClsId(label);
		this.label = label;
		this.modality = modality;
		this.granularity = granularity;
		
		if (method != null)
			this.assemblyMethod = method;
	}

	public String toString()
	{
		return "" + this.getClass().getSimpleName() + ((this.label != null)? ("{Label:" + this.label + "}"):"") +
					 	  "{mod:" + this.modality + "}{Assembly Method:" + this.assemblyMethod + "}";
	}
}
