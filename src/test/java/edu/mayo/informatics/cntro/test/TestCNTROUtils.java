package edu.mayo.informatics.cntro.test;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.mayo.informatics.cntro.utils.CNTROUtils;

public class TestCNTROUtils 
{
	@Test
	public void test() 
	{
		try 
		{
			assertEquals(CNTROUtils.getNumericValueFromString("NINE Months"), 9);
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fail("Failed TestCNTROUtils");
	}

}
