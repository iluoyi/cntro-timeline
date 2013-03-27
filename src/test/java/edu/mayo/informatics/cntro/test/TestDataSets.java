package edu.mayo.informatics.cntro.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.List;

import org.junit.Test;

import edu.mayo.informatics.cntro.exceptions.CNTROException;
import edu.mayo.informatics.cntro.impl.CNTROQueryImpl;
import edu.mayo.informatics.cntro.main.CNTROAuxiliary;
import edu.mayo.informatics.cntro.model.Event;
import edu.mayo.informatics.cntro.model.timeline.CNTROTimeLineEventList;
import edu.mayo.informatics.cntro.queryIF.CNTROQuery;
import edu.mayo.informatics.cntro.queryIF.ParserType;
import edu.mayo.informatics.cntro.utils.CNTROFileUtils;
import edu.mayo.informatics.cntro.utils.CNTROUtils;
import edu.mayo.informatics.cntro.utils.FileComparison;
import edu.mayo.informatics.cntro.utils.Transformer;

public class TestDataSets 
{
	private CNTROQuery query = null;
	private CNTROAuxiliary aux = null;

	private int processed = 0;
	
	public String failed = "";
	
	private boolean createGoldStandardDataSet = false;
	private boolean compareWithGoldStandard = true;

	private CNTROTimeLineEventList cetl = null;
	
	
	@Test
	public void generateAndTestWithGoldStandard() throws CNTROException
	{
		int start = TestCfg.startIndexLimit;
		int end = TestCfg.endIndexLimit;

		//start = 1;
		//end = 292; 
		
		TestDataSets ts = new TestDataSets();
		
		if (createGoldStandardDataSet)
		{
			int processed = ts.runTestDataSet(start, end, "gold");
			System.out.println("Gold Standard Test Data-Set created for " + processed + " files!!");
		}

		int processed = ts.runTestDataSet(start, end, null);
		System.out.println("Test Data-Set processed " + processed + "(out of total=" + TestCfg.existing.length + ") files!!");
		System.out.println((CNTROUtils.isNull(ts.failed))?"No File failed to processed!!":("Failed Files are :" + ts.failed));
		
		if (compareWithGoldStandard)
		{
			System.out.println("\n############################\nComparing with Gold Standard Files...");
			double mismatches = ts.compareWithGoldStandard(start, end, true, true);
			System.out.println("Mismatches=" + mismatches);
			System.out.println("\nComparison DONE!!\n############################\n");
			//double total = (end - start + 1);
			double total = processed;
			double perc = (total - mismatches)/total;
			System.out.println("Total= " + (int) total);
			System.out.println("Mismatches= " + (int) mismatches);
			System.out.printf("Success Rate:  %.2f%n", perc*100);
			System.out.println("\n############################\n");
		}
	}
	
	public int runTestDataSet(int startFileIndex, int endFileIndex, String type)
	{
		int start = (startFileIndex < TestCfg.startIndexLimit)?TestCfg.startIndexLimit : startFileIndex;
		int end = (endFileIndex > TestCfg.endIndexLimit)?TestCfg.endIndexLimit : endFileIndex;
		
		failed = "";
		processed = 0;
		
		File ppxsltFile = new File("src/main/resources/xslt/prettyprint.xsl");
		File xsltFile = new File("src/main/resources/xslt/DataSetTxfm.xsl");
		//System.out.println("xslt file found=" + xsltFile.exists());

		for (int i = start; i <= end; i++)
		{
			if (TestCfg.isMissing(i))
				continue;
			
			aux = null;
			try
			{
				aux = new CNTROAuxiliary(TestCfg.getInputFileUri(i));
				aux.parsertype = ParserType.OWLAPI; // default
				
				try
				{
					aux.loadOntology();
				}
				catch(Exception e)
				{
					System.out.println("File could not be loaded for index:" + i);
					System.out.println(e.getMessage());
					continue;
				}
				
				aux.parse();
				query = new CNTROQueryImpl(aux);
				processed++;
			}
			catch(Exception e)
			{
				System.out.println("Failed to get Test Data Set for Index=" + i);
				System.out.println(e.getMessage());
				failed = (CNTROUtils.isNull(failed))? ("" + i) : (failed + "," + i);
				continue;
			}
			
			String eventsStr = "No Events Found!!";
			
			cetl = new CNTROTimeLineEventList();
			cetl.setNoteFileName(TestCfg.getInputFileUri(i));
			
			if (aux.parser_.getEventCount() > 0)
			{
				Hashtable<String, List<Event>> events = query.getEventsTimeline(false, false, false, true, null);
				eventsStr = TestUtils.marshallTimeLine(events, false, cetl);
			}
			
			try
			{
				CNTROFileUtils.createFileWithContents(TestCfg.getTempXMLFileUri(), eventsStr);
				
				File tempFile = new File(TestCfg.getTempXMLFileUri());
				File xmlFile = new File(TestCfg.getTestDataSetXMLFileUri(i));
				System.out.println("xml file \"" + xmlFile.getAbsolutePath() + "\" found? " + xmlFile.exists());
				
				File csvFile = null;
				if ((type != null)&&(type.indexOf("gold") != -1))
					csvFile = new File(TestCfg.getTestDataSetGoldFileUri(i));
				else
					csvFile = new File(TestCfg.getTestDataSetCSVFileUri(i));
				
				Transformer transformer = new Transformer();
				transformer.transform(new FileInputStream(tempFile), new FileInputStream(ppxsltFile), new FileOutputStream(xmlFile), null);
				transformer.transform(new FileInputStream(xmlFile), new FileInputStream(xsltFile), new FileOutputStream(csvFile), null);
				
				if (tempFile.exists())
					tempFile.delete();
			}
			catch(Exception e)
			{
				System.out.println("Failed to create Test Data Set for Index=" + i);
				System.out.println(e.getMessage());
				failed = (CNTROUtils.isNull(failed))? ("" + i) : (failed + "," + i);
				continue;
			}
		}
		
		return processed;
	}
	
	public int compareWithGoldStandard(int startFileIndex, int endFileIndex, boolean printDifferences, boolean generateReports)
	{
		int mismatches = 0;
		
		int start = (startFileIndex < TestCfg.startIndexLimit)?TestCfg.startIndexLimit : startFileIndex;
		int end = (endFileIndex > TestCfg.endIndexLimit)?TestCfg.endIndexLimit : endFileIndex;
		
		processed = 0;
		
		for (int i = start; i <= end; i++)
		{
			if (TestCfg.isMissing(i))
				continue;
			
			try
			{
				PrintStream ps = null;
				if (generateReports)
				{
					FileOutputStream out; 
					try 
					{
						 // Create a new file output stream
						  out = new FileOutputStream(TestCfg.getTestDataSetMismatchFileUri(i));
						  ps = new PrintStream(out);
						
						  boolean isSame = FileComparison.areEventTimelinesSame(TestCfg.getTestDataSetGoldFileUri(i), TestCfg.getTestDataSetCSVFileUri(i), printDifferences, ps);
						  mismatches = mismatches + ((isSame)?0:1);
						  ps.close();
						  
						  if (isSame)
						  {
							  // Delete the mismatch report is no difference. it must be empty.
							  try
							  {
								  File f=new File(TestCfg.getTestDataSetMismatchFileUri(i));
								  if(f.exists() && f.isFile())
								  {
									  f.delete();
								  }
							  }
							  catch(Exception e)
							  {
								  //e.printStackTrace();
							  }
						  }
							  
					}
					catch (Exception e)
					{
						e.printStackTrace();
						System.err.println ("Error in writing to file" + e);
					}
				}
			}
			catch(Exception e)
			{
				System.out.println("Failed to compare Data Set for Index=" + i);
				System.out.println(e.getMessage());
				continue;
			}
		}
		
		return mismatches;
	}
}
