package edu.mayo.informatics.cntro.utils;

import java.text.ParsePosition;
import java.util.Date;

public class DateParserUtil 
{
	public static Date parse(String text)
	{
		Date retDate = null;
		DateParser dp = new DateParser();
		if (!CNTROUtils.isNull(text))
			retDate = dp.parse(text, new ParsePosition(0));
		
		if (retDate == null)
		{
			String tstr = text.trim();
			
			// replace anything which is not alphbet or number
			tstr = tstr.replaceAll("[^a-zA-Z0-9.]", " ");

			if (tstr.toLowerCase().indexOf("of") != -1)
				tstr = tstr.toLowerCase().replaceAll(" of ", " ");
			
			retDate = dp.parse(tstr, new ParsePosition(0));
		}
		
		return retDate;
	}
}
