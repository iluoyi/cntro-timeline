package edu.mayo.informatics.cntro.model;

import java.util.Date;

import edu.mayo.informatics.cntro.queryIF.Granularity;

public class TimeInstant extends Time
{
	private String originalTime;
	private Date normalizedTime;
	
	public TimeInstant()
	{
		super(null);
		this.originalTime = null;
		this.normalizedTime = null;
	}
	
	public TimeInstant(String label, boolean modality, Granularity granularity,
			String originalTime, Date normalizedTime, TimeAssemblyMethod method) 
	{
		super(label, modality, granularity, method);

		if ((this.assemblyMethod == TimeAssemblyMethod.UNKNOWN)&&
			((originalTime != null)||(normalizedTime != null)))
			this.assemblyMethod = TimeAssemblyMethod.ASSERTED;
		
		this.originalTime = originalTime;
		this.normalizedTime = normalizedTime;
	}

	public Date getNormalizedTime()
	{
		return this.normalizedTime;
	}
	
	public void setNormalizedTime(Date normTime)
	{
		this.normalizedTime = normTime;
	}

	public String toString()
	{
		return "{" + super.toString() + 
				((this.originalTime != null)? ("{Orig:" + this.originalTime + "}"):"")  +
				((this.normalizedTime != null)? ("{Norm:" + this.normalizedTime + "}"):"")  +
				((this.granularity != null)? ("{Gran:" + this.granularity + "}"):"") + "}";
	}
	
	public String getClsId()
	{
		return originalTime;
	}

	public String getOriginalTime() 
	{
		return originalTime;
	}

	public void setOriginalTime(String originalTime) 
	{
		this.originalTime = originalTime;
		this.assemblyMethod = TimeAssemblyMethod.ASSERTED;
	}
}
