package edu.mayo.informatics.cntro.model;

public enum TimeAssemblyMethod 
{
	ASSERTED, 	// User specifed the time 
	INFERRED, 	// Parser or program computed it or inferred it
	ASSIGNED, 	// Program assigned it based on some protocol
	UNKNOWN		// Default
}
