package edu.mayo.informatics.cntro.model;

public class TimePeriod  extends Time
{
	public String description;

	public TimePeriod(String description, TimeAssemblyMethod method) 
	{
		super(method);
		this.description = description;
	}
	
	public String toString()
	{
		return "{" + super.toString() + 
		((this.description != null)? ("{Desc:" + this.description + "}"):"") + "}";
	}
	
	public String getClsId()
	{
		return description;
	}
}
