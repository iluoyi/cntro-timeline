package edu.mayo.informatics.cntro.model;

public class TemporalOffset extends Duration 
{
	public TemporalOffset()
	{
	}
	
	public String toString()
	{
		return ""  + ((this.label != null)? ("{Offset. label:" + this.label + "}"):"") +
		((this.unit != null)? ("{Offset. unit:" + this.unit + "}"):"") +
		"{Offset. value:" + this.value + "}" ;
	}
	
	public String getClsId()
	{
		return super.getClsId();
	}
}
