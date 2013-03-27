package edu.mayo.informatics.cntro.model;

public class CNTROCls 
{
	private String id = null;
	
	public String getClsId()
	{
		return id;
	}

	public void setClsId(String pid)
	{
		this.id = pid;
	}
	
	public String toString()
	{
		return getClsId();
	}
}
