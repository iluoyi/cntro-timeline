package edu.mayo.informatics.cntro.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.StringTokenizer;

public class FileComparison 
{
	public static String defaultFile1 = "src/test/resources/TestDataSet/gold/TDS_3.csv";
	public static String defaultFile2 = "src/test/resources/TestDataSet/csv/TDS_3.csv";
	
	public static String ENTRY_DELIM = ";";
	
	public static void main (String[] args) throws java.io.IOException
    {
		//Getting the name of the files to be compared.
		BufferedReader br2 = new BufferedReader (new
	    InputStreamReader(System.in));
		System.out.println("Enter 1st File name:");
  		String firstFile = br2.readLine();
  		
  		if ((firstFile == null)||("".equals(firstFile.trim())))
  			firstFile = FileComparison.defaultFile1;
  		
  		System.out.println("Enter 2nd File name:");
  		String secondFile = br2.readLine();
  		
  		if ((secondFile == null)||("".equals(secondFile.trim())))
  			secondFile = FileComparison.defaultFile2;
  		
  		System.out.println("Comparing \nFile1=\"" + firstFile + "\" \nwith\n File2=\"" + secondFile + "\"");
  		try
  		{
  			System.out.println("\nAre they Same? " + areEventTimelinesSame(firstFile, secondFile, true, System.out));
  		}
  		catch(Exception e)
  		{
  			e.printStackTrace();
  		}
   }

	public static boolean areEventTimelinesSame(String expectedResultsFile, 
			String actualResultsFile, 
			boolean printDifferencesToPrintStream, 
			PrintStream ps) throws Exception
	{
		return areTwoFileWithMarkers(expectedResultsFile, actualResultsFile, true, "STARTTOKEN", "ENDTOKEN", "DATAMARKER", ps);
	}
	
	private static boolean areTwoFileWithMarkers(String expectedResultsFile, 
												String actualResultsFile, 
												boolean printDifferencesToPrintStream, 
												String startMarker, 
												String endMarker, 
												String middleMarker, 
												PrintStream ps) throws Exception
	{
		boolean isSame = false;
		try
		{
			boolean toPrint = printDifferencesToPrintStream;
			
			if (toPrint && (ps == null))
				toPrint = false;
			
			isSame = areTwoFileWithSameText(expectedResultsFile, actualResultsFile, false, ps);
			// First see if there is any difference in two files with just the text and tokens
			if (!isSame)
			{
		 		String firstFileStr="";
				String secondFileStr="";
				
		        //Reading the contents of the files
		 		BufferedReader firstBufferedReader = new BufferedReader (new FileReader (expectedResultsFile));
		  		BufferedReader secondBufferedReader = new BufferedReader (new FileReader (actualResultsFile));
	
		 		String y="";
				while((y = firstBufferedReader.readLine()) != null)
					firstFileStr += y;
	
		  		String z = "";
		 		while((z = secondBufferedReader.readLine()) != null)
		 			secondFileStr += z;

		 		String defs[] = {"No Event Defs Found"};
		 		
				if ((!CNTROUtils.isNull(startMarker))&&(firstFileStr.indexOf(startMarker) != -1))
				{
					firstFileStr = firstFileStr.split(startMarker)[1];
				}
				
				if ((!CNTROUtils.isNull(endMarker))&&(firstFileStr.indexOf(endMarker) != -1))
				{
					String[] parts = firstFileStr.split(endMarker);
					defs = parts[1].trim().split("EVENT-DESC-");
					firstFileStr = parts[0];
				}
	
				if ((!CNTROUtils.isNull(startMarker))&&(secondFileStr.indexOf(startMarker) != -1))
				{
					secondFileStr = secondFileStr.split(startMarker)[1];
				}
				
				if ((!CNTROUtils.isNull(endMarker))&&(secondFileStr.indexOf(endMarker) != -1))
				{
					secondFileStr = secondFileStr.split(endMarker)[0];
				}
				
				String firstMetrix[][] = null;
				
				int cols = 8;
				int frows = 0;
				if ((!CNTROUtils.isNull(middleMarker))&&(firstFileStr.indexOf(middleMarker) != -1))
				{
					String firstMetrixHdr[] = (firstFileStr.split(middleMarker)[0]).split(",");
					String firstMetrixdata[] = (firstFileStr.split(middleMarker)[1]).split("EVENTID-");
					
					frows = firstMetrixdata.length;
					
					firstMetrix = new String[frows][cols];
					
					firstMetrix[0] = firstMetrixHdr;
					
					int j = 1;
					for (int i = 1; i < frows; i++)
							firstMetrix[i] = firstMetrixdata[j++].split(",");
				}
				
				String secondMetrix[][] = null;
				
				int srows = 0;
				if ((!CNTROUtils.isNull(middleMarker))&&(secondFileStr.indexOf(middleMarker) != -1))
				{
					String secondMetrixHdr[] = (secondFileStr.split(middleMarker)[0]).split(",");
					String secondMetrixdata[] = (secondFileStr.split(middleMarker)[1]).split("EVENTID-");
					
					srows = secondMetrixdata.length;
					
					secondMetrix = new String[srows][cols];
					
					secondMetrix[0] = secondMetrixHdr;
					
					int j = 1;
					for (int i = 1; i < srows; i++)
							secondMetrix[i] = secondMetrixdata[j++].split(",");
				}
				
				if (toPrint)
				{
					ps.println("\nExpected Results File: " + expectedResultsFile);
					ps.println("Expected Results:");
					for (int a = 0; a < frows; a++)
					{
						ps.print("\n");
						for (int b=0; b < cols; b++)
						{
							String val = CNTROUtils.isNull(firstMetrix[a][b])?" ":(firstMetrix[a][b]);
							if (a == 0)
								val = val + " ";
							else
								val = val + "\t";
							ps.print(val);
						}
					}
					
					ps.println("\nComputed Results File: " + actualResultsFile);
					ps.println("Computed Results:");
					for (int a = 0; a < frows; a++)
					{
						ps.print("\n");
						for (int b=0; b < cols; b++)
						{
							String val = CNTROUtils.isNull(secondMetrix[a][b])?" ":(secondMetrix[a][b]);
							if (a == 0)
								val = val + " ";
							else
								val = val + "\t";
							ps.print(val);
						}
					}
				}
				
				isSame = (frows == srows);
								
				if (isSame)
				{
					String def = "";
					for (String f : defs)
						def += "\n" + f;
					
					def = def.substring(def.indexOf("EVENT"));
					if (toPrint)
					{
						ps.println("\nExpected Results File: " + expectedResultsFile);
						ps.println("Computed Results File: " + actualResultsFile);
						def = def.replaceFirst("All Asserted and Inferred Relations", "\nAll Asserted and Inferred Relations");
						def = def.replaceFirst("=========", "\n=========");
						def = def.replaceAll("EVENT-RELATION", "\nEVENT-RELATION");
						def += "\n";
						ps.println("########## EVENTS ############\n" + def + "\n##############################");
					}
					// compare entries now
					for (int a = 1; a < frows; a++)
					{
						for (int b=1; b < cols; b++)
						{
							if ((firstMetrix[a][b] != null)&&(secondMetrix[a][b] != null)&&(!isTwoEntriesSame(firstMetrix[a][b],secondMetrix[a][b])))
							{
								isSame = false;
								if (toPrint)
								{
									ps.println("\nExpected: Event [ " + firstMetrix[a][0] + " ] --> [ " + firstMetrix[0][b] + " ] --> Event [ " + ((CNTROUtils.isNull(firstMetrix[a][b]))? " " : firstMetrix[a][b]) + " ]");
									ps.println("Found: Event [ " + secondMetrix[a][0] + " ] --> [ " + secondMetrix[0][b] + " ] --> Event [ " + ((CNTROUtils.isNull(secondMetrix[a][b]))? " " : secondMetrix[a][b]) + " ]");
								}
							}
						}
					}
				}
				else
				{
					if (toPrint)
						ps.println("Different number of events. Not same.");
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return isSame;
 	}
	
	public static boolean isTwoEntriesSame(String first, String second)
	{
		if ((CNTROUtils.isNull(first))&&(CNTROUtils.isNull(second)))
			return true;

		if ((!CNTROUtils.isNull(first))&&(CNTROUtils.isNull(second)))
			return false;

		if ((CNTROUtils.isNull(first))&&(!CNTROUtils.isNull(second)))
			return false;
		
		String[] fa = first.split(ENTRY_DELIM);
		String[] sa = second.split(ENTRY_DELIM);
		
		if (sa.length != fa.length)
			return false;

		try
		{
			boolean found = true;
			for (String fe : fa)
			{
				if (!found)
					return false;
				
				found = false;
				for (String se : sa)
					if (((!CNTROUtils.isNull(fe))&&(!CNTROUtils.isNull(se)))&&
						(Integer.parseInt(fe.trim()) == Integer.parseInt(se.trim())))
					{
							found = true;
							break;
					}
			}

			found = true;
			
			for (String se : sa)
			{
				if (!found)
					return false;
				
				found = false;
				for (String fe : fa)
					if (((!CNTROUtils.isNull(fe))&&(!CNTROUtils.isNull(se)))&&
						(Integer.parseInt(fe.trim()) == Integer.parseInt(se.trim())))
					{
							found = true;
							break;
					}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static boolean areTwoFileWithSameText(String firstFile, String secondFile, boolean printDifferencesToPrintStream, PrintStream ps) throws Exception
	{
  		String firstFileStr="";
		String secondFileStr="";
		
        //Reading the contents of the files
 		BufferedReader firstBufferedReader = new BufferedReader (new FileReader (firstFile));
  		BufferedReader secondBufferedReader = new BufferedReader (new FileReader (secondFile));

  		String z = "";
 		while((z=secondBufferedReader.readLine())!=null)
 			firstFileStr+=z;

 		String y="";
		while((y=firstBufferedReader.readLine())!=null)
  			secondFileStr+=y;
		
        //String tokenizing
  		int secondFileTokenCounter = 0;
  		StringTokenizer secondTokenizer = new StringTokenizer (secondFileStr);
  		String[] secondFileTokens = new String[100000];
  		for(int l=0;l<100000;l++)
	   	{
  			secondFileTokens[l]="";
  		}
  		
  		String s2="";
  		int i=0;
  		while (secondTokenizer.hasMoreTokens())
    	{
      		s2 = secondTokenizer.nextToken();
     	    secondFileTokens[i]=s2;
			i++;
            secondFileTokenCounter++;
    	}

     	int firstFileTokenCounter = 0;
	   	StringTokenizer firstTokenizer = new StringTokenizer (firstFileStr);
	   	String[] firstFileTokens = new String[100000];
	   	for(int k=0;k<100000;k++)
	   	{
	   		firstFileTokens[k]="";
	   	}
	   	
	   	String s1 = "";
	   	int j=0;
	   	while (firstTokenizer.hasMoreTokens())
	    {
	       s1 = firstTokenizer.nextToken();
	       firstFileTokens[j] = s1;
	       j++;
	       firstFileTokenCounter++;
	    }

	   	//comparing the contents of the files and printing the differences, if any.
		int x=0;
     	for(int m=0;m<secondFileTokens.length;m++)
     	{
			if(secondFileTokens[m].trim().equals(firstFileTokens[m].trim()))
			{
				
			}
		 	else
		 	{
		 		x++;
		 		if (printDifferencesToPrintStream)
		 		{
		 			ps.println(secondFileTokens[m] + " -- " +firstFileTokens[m]);
		 			ps.println();
		 		}
		    }
		}
     	
 		if (printDifferencesToPrintStream)
 		{
     		if(x>0)
     		{
     			ps.println("Files are not equal");
     		}
     		else
     			ps.println("Files are equal. No difference found");
 		}
 		
 		return (x == 0);
 	}
}



