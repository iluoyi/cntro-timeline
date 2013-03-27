package edu.mayo.informatics.cntro.model;

import edu.mayo.informatics.cntro.queryIF.TemporalRelationType;

public class TemporalRelation  extends CNTROCls
{
	// If the relation is read from source
	// A false value will indicate that it is either
	// computed or inferred from native relations
	public boolean isNative = true;
	
	public Event sourceEvent;
	public TemporalRelationType relation;
	public Time targetTime;
	public Event targetEvent;
	public TemporalOffset offset;
	
	public TemporalRelation(Event sourceEvent, TemporalRelationType relation,
			Time targetTime, Event targetEvent) 
	{
		super();
		this.sourceEvent = sourceEvent;
		this.relation = relation;
		this.targetTime = targetTime;
		this.targetEvent = targetEvent;
	}

	public TemporalRelation(Event sourceEvent, TemporalRelationType relation,
			Time targetTime, Event targetEvent, TemporalOffset offset) 
	{
		super();
		this.sourceEvent = sourceEvent;
		this.relation = relation;
		this.targetTime = targetTime;
		this.targetEvent = targetEvent;
		this.offset = offset;
	}
	
	public String toString()
	{
		return "{Relation:\n\t\t{SrcEvt:" + ((this.sourceEvent == null)? this.sourceEvent : this.sourceEvent.getClsId()) + "}\n\t\t\t ******* {" + 
			   this.relation + "} *******\n" + 
			   ((this.targetEvent == null)? "" : "\t\t{TrgEvt:" + this.targetEvent.getClsId() + "}\n") +
			   ((this.targetTime == null)?"":"\t\t\t{Time:"+this.targetTime + "}\n") +
			   ((this.offset != null)? ("\t\t\t{Offset:" + this.offset) + "}" : "");
	}
	
	public String getClsId()
	{
		if (super.getClsId() != null)
			return super.getClsId();
		
		return "" + ((this.sourceEvent != null)? this.sourceEvent.getClsId() : "NULL") +
					((this.relation != null)? ("-" + this.relation.name() + "-") : "") +
					((this.targetEvent != null)? this.targetEvent.getClsId() : "") +
					((this.targetTime != null)? this.targetTime.getClsId() : "");
					
	}
}