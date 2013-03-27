package edu.mayo.informatics.cntro.main;


public class CNTROConstants 
{
	public static final String CNTRO_TR_NS = "http://informatics.mayo.edu/CNTemporalRelation#";
	public static final String CNTRO_NS = "http://informatics.mayo.edu/CNTRO#";
	//public static final String AELST_NS = "http://www.cntro.org/AE-lst.owl#";
	
	public static final String AELST_NS = CNTRO_NS;
	
	// Class Names
	public static final String CNTRO_EVENT_CLS_NAME = "Event";
	public static final String CNTRO_TIMEINSTANCE_CLS_NAME = "TimeInstant";
	public static final String CNTRO_TIMEINTERVAL_CLS_NAME = "TimeInterval";
	public static final String CNTRO_TIMEPERIOD_CLS_NAME = "TimePeriod";
	public static final String CNTRO_TIMEPHASE_CLS_NAME = "TimePhase";
	public static final String CNTRO_DURATION_CLS_NAME = "Duration";

	public static final String CNTRO_TR_TEMPORAL_RELATION_STMT_CLS_NAME = "TemporalRelationStatement";
	
	// Property Names
	public static final String CNTRO_HASVALIDTIME_PRP_NAME = "hasValidTime";
	public static final String CNTRO_HASNOTETIME_PRP_NAME = "hasNoteTime";
	public static final String CNTRO_HASPERIOD_PRP_NAME = "hasPeriod";
	public static final String CNTRO_TEMPORALRELATION_PRP_NAME = "temporalRelation";
	public static final String CNTRO_HASORIGINALTIME_PRP_NAME = "hasOrigTime";
	public static final String CNTRO_HASNORMALIZEDTIME_PRP_NAME = "hasNormalizedTime";
	public static final String CNTRO_HASMODALITY_PRP_NAME = "hasModality";
	public static final String CNTRO_HASSTARTTIME_PRP_NAME = "hasStartTime";
	public static final String CNTRO_HASENDTIME_PRP_NAME = "hasEndTime";
	public static final String CNTRO_HASDURATION_PRP_NAME = "hasDuration";
	public static final String CNTRO_HASDURATIONVALUE_PRP_NAME = "hasDurationValue";
	public static final String CNTRO_HASDURATIONUNIT_PRP_NAME = "hasDurationUnit";
	public static final String CNTRO_HASOFFSET_PRP_NAME = "hasOffset";
	public static final String CNTRO_HASTIMEOFFSET_PRP_NAME = "hasTimeOffset";
	public static final String CNTRO_HASGRANULARITY_PRP_NAME = "hasGranularity";
	
	public static final String CNTRO_TR_AFTER_PRP_NAME = "after";
	public static final String CNTRO_TR_BEFORE_PRP_NAME = "before";
	public static final String CNTRO_TR_MEET_PRP_NAME = "meet";
	public static final String CNTRO_TR_OVERLAP_PRP_NAME = "overlap";
	public static final String CNTRO_TR_CONTAIN_PRP_NAME = "contain";
	public static final String CNTRO_TR_DURING_PRP_NAME = "during";
	public static final String CNTRO_TR_EQUAL_PRP_NAME = "equal";
	public static final String CNTRO_TR_FINISH_PRP_NAME = "finish";
	public static final String CNTRO_TR_START_PRP_NAME = "start";
	
	public static final String CNTRO_TR_CONTINUES_THROUGH_PRP_NAME = "continues_through";
	public static final String CNTRO_TR_INCLUDE_PRP_NAME = "include";
	public static final String CNTRO_TR_INITIATE_PRP_NAME = "initiate";
	public static final String CNTRO_TR_IS_INCLUDED_PRP_NAME = "is_included";
	
	public static final String CNTRO_TR_OVERLAPPED_BY_PRP_NAME = "overlapped_by";
	public static final String CNTRO_TR_SIMULTANEOUS_PRP_NAME = "simultaneous";
	public static final String CNTRO_TR_TERMINATE_PRP_NAME = "terminate";
	public static final String CNTRO_TR_SAMEAS_PRP_NAME = "sameas";
	
	
	public static final String CNTRO_TR_TEMPORAL_SUBJECT_PRP_NAME = "temporalSubject";
	public static final String CNTRO_TR_TEMPORAL_OBJECT_PRP_NAME = "temporalObject";
	public static final String CNTRO_TR_TEMPORAL_PREDICATE_PRP_NAME = "temporalPredicate";

	
	// Classes
	public static final String CNTRO_EVENT_CLS = getWithNS(CNTRO_EVENT_CLS_NAME);
	public static final String CNTRO_TIMEINSTANCE_CLS = getWithNS(CNTRO_TIMEINSTANCE_CLS_NAME);
	public static final String CNTRO_TIMEINTERVAL_CLS = getWithNS(CNTRO_TIMEINTERVAL_CLS_NAME);
	public static final String CNTRO_TIMEPERIOD_CLS = getWithNS(CNTRO_TIMEPERIOD_CLS_NAME);
	public static final String CNTRO_TIMEPHASE_CLS = getWithNS(CNTRO_TIMEPHASE_CLS_NAME);
	public static final String CNTRO_DURATION_CLS = getWithNS(CNTRO_DURATION_CLS_NAME);
	
	public static final String CNTRO_TR_TEMPORAL_RELATION_STMT_CLS = getWithNS(CNTRO_TR_TEMPORAL_RELATION_STMT_CLS_NAME);
	
	// Properties
	public static final String CNTRO_HASVALIDTIME_PRP = getWithNS(CNTRO_HASVALIDTIME_PRP_NAME, AELST_NS);
	public static final String CNTRO_HASNOTETIME_PRP = getWithNS(CNTRO_HASNOTETIME_PRP_NAME, AELST_NS);
	public static final String CNTRO_HASPERIOD_PRP = getWithNS(CNTRO_HASPERIOD_PRP_NAME, AELST_NS);
	public static final String CNTRO_TEMPORALRELATION_PRP = getWithNS(CNTRO_TEMPORALRELATION_PRP_NAME, CNTRO_TR_NS);
	public static final String CNTRO_HASORIGINALTIME_PRP = getWithNS(CNTRO_HASORIGINALTIME_PRP_NAME, AELST_NS);
	public static final String CNTRO_HASNORMALIZEDTIME_PRP = getWithNS(CNTRO_HASNORMALIZEDTIME_PRP_NAME, AELST_NS);
	public static final String CNTRO_HASMODALITY_PRP = getWithNS(CNTRO_HASMODALITY_PRP_NAME, AELST_NS);
	public static final String CNTRO_HASSTARTTIME_PRP = getWithNS(CNTRO_HASSTARTTIME_PRP_NAME, AELST_NS);
	public static final String CNTRO_HASENDTIME_PRP = getWithNS(CNTRO_HASENDTIME_PRP_NAME, AELST_NS);
	public static final String CNTRO_HASDURATION_PRP = getWithNS(CNTRO_HASDURATION_PRP_NAME);
	public static final String CNTRO_HASDURATIONVALUE_PRP = getWithNS(CNTRO_HASDURATIONVALUE_PRP_NAME);
	public static final String CNTRO_HASDURATIONUNIT_PRP = getWithNS(CNTRO_HASDURATIONUNIT_PRP_NAME);
	public static final String CNTRO_HASOFFSET_PRP = getWithNS(CNTRO_HASOFFSET_PRP_NAME, AELST_NS);
	public static final String CNTRO_HASTIMEOFFSET_PRP = getWithNS(CNTRO_HASTIMEOFFSET_PRP_NAME, AELST_NS);
	
	public static final String CNTRO_TR_AFTER_PRP = getWithNS(CNTRO_TR_AFTER_PRP_NAME);
	public static final String CNTRO_TR_BEFORE_PRP = getWithNS(CNTRO_TR_BEFORE_PRP_NAME);
	public static final String CNTRO_TR_MEET_PRP = getWithNS(CNTRO_TR_MEET_PRP_NAME);
	public static final String CNTRO_TR_OVERLAP_PRP = getWithNS(CNTRO_TR_OVERLAP_PRP_NAME);
	public static final String CNTRO_TR_CONTAIN_PRP = getWithNS(CNTRO_TR_CONTAIN_PRP_NAME);
	public static final String CNTRO_TR_DURING_PRP = getWithNS(CNTRO_TR_DURING_PRP_NAME);
	public static final String CNTRO_TR_EQUAL_PRP = getWithNS(CNTRO_TR_EQUAL_PRP_NAME);
	public static final String CNTRO_TR_FINISH_PRP = getWithNS(CNTRO_TR_FINISH_PRP_NAME);
	public static final String CNTRO_TR_START_PRP = getWithNS(CNTRO_TR_START_PRP_NAME);
	
	public static final String CNTRO_TR_CONTINUES_THROUGH_PRP = getWithNS(CNTRO_TR_CONTINUES_THROUGH_PRP_NAME);
	public static final String CNTRO_TR_INCLUDE_PRP = getWithNS(CNTRO_TR_INCLUDE_PRP_NAME);
	public static final String CNTRO_TR_INITIATE_PRP = getWithNS(CNTRO_TR_INITIATE_PRP_NAME);
	public static final String CNTRO_TR_IS_INCLUDED_PRP = getWithNS(CNTRO_TR_IS_INCLUDED_PRP_NAME);
	public static final String CNTRO_TR_OVERLAPPED_BY_PRP = getWithNS(CNTRO_TR_OVERLAPPED_BY_PRP_NAME);
	public static final String CNTRO_TR_SIMULTANEOUS_PRP = getWithNS(CNTRO_TR_SIMULTANEOUS_PRP_NAME);
	public static final String CNTRO_TR_TERMINATE_PRP = getWithNS(CNTRO_TR_TERMINATE_PRP_NAME);
	public static final String CNTRO_TR_SAMEAS_PRP = getWithNS(CNTRO_TR_SAMEAS_PRP_NAME);

	public static final String CNTRO_TR_TEMPORAL_SUBJECT_PRP = getWithNS(CNTRO_TR_TEMPORAL_SUBJECT_PRP_NAME, AELST_NS);
	public static final String CNTRO_TR_TEMPORAL_OBJECT_PRP = getWithNS(CNTRO_TR_TEMPORAL_OBJECT_PRP_NAME, AELST_NS);
	public static final String CNTRO_TR_TEMPORAL_PREDICATE_PRP = getWithNS(CNTRO_TR_TEMPORAL_PREDICATE_PRP_NAME, AELST_NS);

	public static String getWithNS(String name)
	{
		return CNTRO_NS + name;
	}
	
	public static String getWithNS(String name, String namespace)
	{
		return namespace + name;
	}
}
