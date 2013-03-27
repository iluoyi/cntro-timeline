package edu.mayo.informatics.cntro.test;

import edu.mayo.informatics.cntro.model.timeline.types.TemporalRelation;

public class TestCfg 
{
	private static String baseDirUri = "src/test/resources/";
	private static String baseTestDirUri = "src/test/resources/TestDataSet/";
	//private static String dataSetUri = baseDirUri + "ReviewedByKimJuly2012/" ; 
	private static String dataSetUri = baseDirUri + "NormalizedAnnotatedEvents/";
	private static String testXMLDataSetUri = baseTestDirUri + "currentRunXML/";
	private static String testCSVDataSetUri = baseTestDirUri + "currentRunCSV/";
	private static String testGoldStandardDataSetUri = baseTestDirUri + "gold/";
	private static String testmismatchesDataSetUri = baseTestDirUri + "mismatches/";
	
	private static String owlfileNameSuff = ".txt.owl";
	private static String dsfileNamePref = "TDS_";
	private static String dsfileNameSuff = ".xml";
	private static String csvfileNameSuff = ".csv";
	private static String reportNameSuff = ".txt";

	public static final int startIndexLimit = 1;
	public static final int endIndexLimit = 295;

	public static String ENTRY_DELIM = ";";

	public static TemporalRelation[] rels = {TemporalRelation.BEFORE, 
			TemporalRelation.DURING, 
			TemporalRelation.EQUAL, 
			TemporalRelation.FINISH, 
			TemporalRelation.MEET, 
			TemporalRelation.OVERLAP, 
			TemporalRelation.START};

	public static final int[] existing = {1,10,100,101,102,103,105,107,108,109,11,110,111,112,113,114,115,116,
									 117,118,119,12,120,121,122,123,124,125,126,127,129,13,132,134,135,136,
									 137,138,139,14,141,142,143,144,145,146,147,148,149,150,151,152,153,154,
									 155,156,157,158,16,160,161,162,163,164,165,166,167,168,169,17,170,171,
									 172,174,175,178,179,18,180,181,182,186,187,188,189,190,191,192,193,194,
									 195,196,197,198,199,2,200,201,202,203,205,206,207,208,209,210,211,212,213,
									 214,215,216,217,218,219,22,220,221,222,223,225,227,228,229,23,230,231,232,
									 233,235,237,238,239,24,241,243,244,246,247,248,249,25,251,252,253,254,255,
									 256,257,258,26,260,263,264,265,267,268,269,27,270,271,272,274,275,276,277,
									 278,279,28,280,282,283,285,286,288,289,29,290,292,294,3,32,33,35,36,37,
									 39,4,41,42,43,44,45,46,47,48,5,50,51,52,53,54,55,57,58,59,6,60,61,62,65,67,
									 69,7,71,72,73,74,75,76,77,78,79,80,82,84,85,86,87,88,89,9,90,91,94,96,98,99};

	public static String getInputFileUri(int fileIndex)
	{
		return dataSetUri + fileIndex + owlfileNameSuff;
	}
	
	public static String getTestDataSetXMLFileUri(int fileIndex)
	{
		return testXMLDataSetUri + dsfileNamePref + fileIndex + dsfileNameSuff;
	}
	
	public static String getTempXMLFileUri()
	{
		return testXMLDataSetUri + "temp" + dsfileNameSuff;
	}
	
	public static String getTestDataSetCSVFileUri(int fileIndex)
	{
		return testCSVDataSetUri + dsfileNamePref + fileIndex + csvfileNameSuff;
	}
	
	public static String getTestDataSetGoldFileUri(int fileIndex)
	{
		return testGoldStandardDataSetUri + dsfileNamePref + fileIndex + csvfileNameSuff;
	}
	
	public static String getTestDataSetMismatchFileUri(int fileIndex)
	{
		return testmismatchesDataSetUri + dsfileNamePref + fileIndex + reportNameSuff;
	}
	
	public static boolean isMissing(int index)
	{
		for(int m : existing)
		if (m == index)
		return false;
		
		return true;
	}
}
