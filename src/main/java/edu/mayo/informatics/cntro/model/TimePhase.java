package edu.mayo.informatics.cntro.model;

public class TimePhase extends TimeInterval 
{
	public TimePeriod period;

	public TimePhase(TimePeriod period, TimeAssemblyMethod method) 
	{
		super(method);
		this.period = period;
	}
	
	public String toString()
	{
		return super.toString();
	}
	
	public String getClsId()
	{
		return super.getClsId() + period.getClsId();
	}
}
