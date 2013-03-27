package edu.mayo.informatics.cntro.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.main.CNTROAuxiliary;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.queryIF.CNTROQuery;
import edu.mayo.informatics.cntro.queryIF.Granularity;
import edu.mayo.informatics.cntro.queryIF.ParserType;

public class TestOWLAPI_Duration_144
{
	private CNTROQuery query = null;
	private CNTROAuxiliary aux = null;
	
	private int DurationTestFile1 = 144;
	
	@Before
	public void setUp() throws Exception 
	{
		try 
		{
			System.out.println("TestOWLAPI_Duration: SetUp() File " + DurationTestFile1);
			aux = new CNTROAuxiliary(TestCfg.getInputFileUri(DurationTestFile1));
			aux.parsertype = ParserType.OWLAPI; // default
			aux.loadOntology();
			aux.parse();
			query = new KimCNTROQueryImpl(aux);
			System.out.println("Setup Done!");
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Ignore
	public void durationBetweenYears()
	{
		assertTrue(aux != null);
		assertTrue(query != null);
		assertTrue(aux.parser_.getEventCount() == 7);
		
		Event startEvent = TestUtils.getEvent(query, "A 2.75X12MM TAXUS EXPRESS2 STENT WAS DEPLOYED", false);
		assertFalse(startEvent == null);

		Event endEvent = TestUtils.getEvent(query, "THE RCA AND LCX WERE FOUND TO BE OCCLUDED WITH THROMBUS", false);
		assertFalse(endEvent == null);
		
		try 
		{
			long duration = query.getDurationBetweenEvents(startEvent, endEvent, Granularity.DAY);
			assertTrue(duration == 1461);
			duration = query.getDurationBetweenEvents(startEvent, endEvent, Granularity.MONTH);
			assertTrue(duration == 42);
			duration = query.getDurationBetweenEvents(startEvent, endEvent, Granularity.YEAR);
			assertTrue(duration == 4);
		} 
		catch (CNTROException e) 
		{
			e.printStackTrace();
			fail();
		}
	}
}
