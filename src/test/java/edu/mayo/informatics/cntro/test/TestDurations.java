package edu.mayo.informatics.cntro.test;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;

import edu.mayo.informatics.cntro.main.CNTROAuxiliary;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.queryIF.ParserType;
import edu.mayo.informatics.cntro.utils.CNTROUtils;

public class TestDurations 
{
	private int total = 0;
	private int readError = 0;
	
	public Hashtable<String, String[]> durationMap = new Hashtable<String, String[]>();
	
	@Before
	public void setUp() throws Exception 
	{
		  try
		  {
			  FileInputStream durationsFile = new FileInputStream("src/test/resources/durations_expected.txt");
			  DataInputStream in = new DataInputStream(durationsFile);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  while ((strLine = br.readLine()) != null)   
			  {
				  total++;
				  if (strLine == null)
					  continue;
				  
				  String[] testValues = strLine.split(",");
				  
				  if ((testValues == null)||(testValues.length < 4))
				  {
					  readError++;
					  System.out.println ("READ ERROR=" + strLine);
					  continue;
				  }
				  
				  durationMap.put(testValues[0], testValues);
			  }
			  //Close the input stream
			  in.close();
			  System.out.println ("Items to Process=" + this.durationMap.size());
		  }
		  catch (Exception e)
		  {
			  System.err.println("Error: " + e.getMessage());
		  }
	}
	
	// if testForThisFile < 0 it will test for all files.
	int [] testForThisFile = 
			{-1};
			 //{32, 125, 206, 129};
			//{239};
	
	int [] results = new int[testForThisFile.length];
	
	@Test
	public void testDurationsAntiplateletTherapy() 
	{
		for (int fn : testForThisFile)
		{
			testDurations(fn, "Antiplatelet Therapy", "StartAntiplateletTherapy", "EndAntiplateletTherapy", 1, 2, true);
			
			//if (duration1 == -1)
				//testDurations(fn, "Antiplatelet Therapy", "InitialDrugElutingStentImplantation", "EndAntiplateletTherapy", 1, 2);
		}
		//testDurations("Implantation and LST", "InitialDrugElutingStentImplantation", "Late_Stent_Thrombosis");
	}
	
	@Test
	public void testDurationsImplantationAndLST() 
	{
		int i = 0;
		//testDurations("Antiplatelet Therapy", "StartAntiplateletTherapy", "EndAntiplateletTherapy");
		for (int fn : testForThisFile)
		{
			results[i++] = testDurations(fn, "Implantation and LST", "InitialDrugElutingStentImplantation", "Late_Stent_Thrombosis", 3, 4, false);
		}
		
		for (int k =0; k < testForThisFile.length; k++)
		{
			System.out.print("tests File No:" + testForThisFile[k]);
			System.out.println("\trestuls:" + results[k]);
		}
	}

	String resultSummaryForFailedCases;
	String resultSummaryForPassedCases;

	// If testFileNo is < 0 then it will test all files, otherwise only the one file specified.
	private int testDurations(int testFileNo, String typeOfTest, String startNormalizedEvent, String endNormalizedEvent, int searchStringIndex, int expectedStringIndex, boolean tryAgain) 
	{
		int processed = 0;
		int failedCases = 0;
		int passedCases = 0;
		resultSummaryForFailedCases = "";
		resultSummaryForPassedCases = "";
		try
		{
			System.out.println ("Testing Duration for : " + typeOfTest + " ...");
			
			if (testFileNo > 0)
				System.out.println("Testing for only FileNo=" + testFileNo);
			
			for (String fileNo : this.durationMap.keySet())
			{
				String searchCondition = null;
				String expectedResult = null;
				
				String[] testCaseArray = this.durationMap.get(fileNo);
				searchCondition = testCaseArray[searchStringIndex];
				expectedResult = testCaseArray[expectedStringIndex];
				
				if ((CNTROUtils.isNull(searchCondition))||(CNTROUtils.isNull(expectedResult)))
					continue;
				
				int currentFileNo = Integer.parseInt(fileNo);
				
				// See if test needs to run only for one file suppled in the parameter.
				if ((testFileNo > 0)&&(currentFileNo != testFileNo))
					continue;
				
				init(currentFileNo);
				assertNotNull(aux);
				assertNotNull(query);
				
				Collection<Event> events = aux.parser_.getEvents();
				Event startEvent = null;
				Event endEvent = null;
				
				for (Event e:events)
				{
					if ((startEvent == null) &&(e.normalizedEventCategory.equals(startNormalizedEvent)))
					{
						startEvent = e;
						continue;
					}
					
					if ((endEvent == null) &&(e.normalizedEventCategory.equals(endNormalizedEvent)))
					{
						endEvent = e;
						continue;
					}
				}
				

				if ((startEvent == null)&&(endEvent == null))
					continue;
				
				long comp = compute(startEvent, endEvent, Granularity.MONTH);
				long expected = Long.parseLong(expectedResult);

				String didWetry2Times = "";
				if ((!matchedExpectations(comp, expected))&&(tryAgain))
				{
					for (Event e2:events)
					{
						if (e2.normalizedEventCategory.equals("InitialDrugElutingStentImplantation"))
						{
							startEvent = e2;
							continue;
						}
					}
					
					if (startEvent != null)
					{
						didWetry2Times = "[RETRY]";
						comp = compute(startEvent, endEvent, Granularity.MONTH);
					}
				}
				
				processed++;
				String resultString = "\n" + didWetry2Times + "File:(" + fileNo + ") ==> Search:(" + searchCondition + ") Expected Result:(" + expectedResult +") Computed:(" + comp + ")";
				
				if (matchedExpectations(comp, expected))
				{
					passedCases++;
					resultSummaryForPassedCases += resultString;
				}
				else
				{
					failedCases++;
					resultSummaryForFailedCases += resultString;
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
		}
		  
		System.out.println("1Total =" + total);
		System.out.println("1Read errors =" + readError);
		System.out.println("1Processed =" + processed);
		System.out.println("1Failed =" + failedCases);
		
		double total = processed;
		double mismatches = failedCases;
		if (processed > 0)
		{
			System.out.printf("\n1Accuracy (percentage) = %.2f%%%n", ((total - mismatches)/total)*100);
			System.out.println("\n\nPASSED CASES:" + passedCases + resultSummaryForPassedCases);
			System.out.println("\n\nFAILED CASES:" + failedCases + resultSummaryForFailedCases);
		}
		
		return passedCases;
	}
	
	private boolean matchedExpectations(long computed, long expected)
	{
		if (computed == expected)
			return true;

		return false;
	}
	
	private long compute(Event startEvent, Event endEvent, Granularity granularity)
	{
		long computed = -1;
		try
		{
			if ((startEvent != null)&&(endEvent != null))
			{
				computed = query.getDurationBetweenEvents(startEvent, endEvent, granularity);
			
				System.out.println("Computed for two non-null time values");
			}
		}
		catch(Exception ce1)
		{
			System.out.println("Failed to computed duration using start and end event" + ce1.getMessage());
		}
		
		if ((startEvent != null)&&(!(computed > 0)))
		{
			try
			{
				computed = query.getDuration(startEvent, false);
			}
			catch(Exception ce2)
			{
				System.out.println("Failed to compute duration using getDuration()" + ce2.getLocalizedMessage());
			}
		}
		
		if ((endEvent != null)&&(!(computed > 0)))
		{
			try
			{
				computed = query.getDuration(endEvent, false);
			}
			catch(Exception ce3)
			{
				System.out.println("Failed to compute duration using getDuration()" + ce3.getLocalizedMessage());
			}
		}
		
		return computed;
	}
	
	/*
	@Ignore
	public void testDurationsImplantationAndLST() 
	{
		int processed = 0;
		int failedCases = 0;
		try
		{
			System.out.println ("Testing Duration of Implantation and LST...");
			processed=0;
			for (String fileNo2 : this.durationMap.keySet())
			{
				String[] testCaseArray = this.durationMap.get(fileNo2);
				String searchCondition = testCaseArray[3];
				String expectedResult = testCaseArray[4];
				
				if ((CNTROUtils.isNull(searchCondition))||(CNTROUtils.isNull(expectedResult)))
					continue;
				
				init(Integer.parseInt(fileNo2));
				assertNotNull(aux);
				assertNotNull(query);

				int computed = 0;
				
				processed++;
				int expected = Integer.parseInt(expectedResult);
				System.out.println("2File:(" + fileNo2 + ") ==> Search:(" + searchCondition + ") Expected Result:(" + expectedResult +")");
				assertEquals(computed, expected);
			}
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
		}
		  
		System.out.println("2Total =" + total);
		System.out.println("2Read errors =" + readError);
		System.out.println("2Processed =" + processed);
		System.out.println("2Failed =" + failedCases);
		if (processed > 0)
			System.out.println("Accuracy =" + ((processed - failedCases)/processed)*100 + "%");
	}
	*/
	
	private CNTROAuxiliary aux = null;
	private KimCNTROQueryImpl query = null;
	
	private void init(int fileNumber)
	{
		System.out.println("Loading File:" + fileNumber);
		try
		{
			aux = new CNTROAuxiliary(TestCfg.getInputFileUri(fileNumber));
			aux.parsertype = ParserType.OWLAPI; // default
			aux.loadOntology();
			aux.parse();
			query = new KimCNTROQueryImpl(aux);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
