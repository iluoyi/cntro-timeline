package edu.mayo.informatics.cntro.utils;

/************************************************************************
 *               Copyright (C) 2004-2005 Mark Greenwood                 *
 *       Developed by Mark Greenwood <m.greenwood@dcs.shef.ac.uk>       *
 *                                                                      *
 * This program is free software; you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation; either version 2 of the License, or    *
 * (at your option) any later version.                                  *
 *                                                                      *
 * This program is distributed in the hope that it will be useful,      *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of       *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
 * GNU General Public License for more details.                         *
 *                                                                      *
 * You should have received a copy of the GNU General Public License    *
 * along with this program; if not, write to the Free Software          *
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.            *
 ************************************************************************/


import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateParser
{
	private static List months = null;
	private static List shortMonths = null;
	private Date today;

	private static int currentParseGranularity = Calendar.DATE;
	
	private static final Pattern[] p = new Pattern[]{
		Pattern.compile("(\\w+)(-)*(\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(\\w+) (\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(?:(\\w+)(?:,|\\s+the)?\\s+)?(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?(\\w+),?\\s+(\\d{4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(?:(\\w+)(?:,|\\s+the)?\\s+)?-?(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?(\\w+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(?:(\\w+)(?:,|\\s+the)?\\s+)?(\\w+)\\.?\\s+?(?:the\\s+)?(\\d{1,2})(?:st|nd|rd|th)?,?\\s+?(\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(?:(\\w+)(?:,|\\s+the)?\\s+)?(\\w+)\\.?\\s+(?:the\\s+)?(\\d{1,2})(?:st|nd|rd|th)?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(\\d{1,2})\\/(\\d{1,2})\\/(\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(\\d{1,2})-(\\d{1,2})-(\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(\\d{1,2})-(\\d{1,2})-(\\d{2})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("Jan(uary)?|Mar(ch)?|Jul(y)?|Aug(ust)?|Oct(ober)?|Dec(ember)?||Apr(il)?|Feb(ruary)?|Sept(ember)?|Nov(ember)?|May|Jun(e)?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		//Pattern.compile("Feb-23-2007", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
		Pattern.compile("(\\w+)-(\\d{1,2})-(\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("^(19|20)\\d{2}$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(\\d{1,2})\\/(\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(\\d{1,2})-(\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
		Pattern.compile("(\\w+)(-)*of (\\d{2,4})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
	};
//((Jan(uary)?|Mar(ch)?|Jul(y)?|Aug(ust)?|Oct(ober)?|Dec(ember)?||Apr(il)?|(Feb(ruary)?|(Sept(ember)?|Nov(ember)?)|May|Jun(e)?)
	private static List weekdays = null;
	private static List shortWeekdays = null;

	private static List eras = null;

	static
	{
		DateFormatSymbols dfs = new DateFormatSymbols(Locale.US);

		months = arrayToList(dfs.getMonths());
		shortMonths = arrayToList(dfs.getShortMonths());

		weekdays = arrayToList(dfs.getWeekdays());
		shortWeekdays = arrayToList(dfs.getShortWeekdays());

		//Is initialised even though it isn't used yet
		eras = arrayToList(dfs.getEras());
	}
	
	public void setToday(Date d){
		today = d;
	}

	public Date parse(String text, ParsePosition pos)
	{
		currentParseGranularity = Calendar.DATE;
		return parse(text, pos, new Date());
	}
	
	public int getDateGranularity()
	{
		return currentParseGranularity;
	}
	
	public Date parse(String timeUnit, int amount, Date date){
		Calendar cal = Calendar.getInstance(Locale.US);

		if (date != null)
			cal.setTime(date);
		String lcase = timeUnit.toLowerCase();
		
		if(lcase.equals("year")){
			cal.roll(Calendar.YEAR, amount);
			return cal.getTime();
		
		}
		return cal.getTime();
	}

	public Date parse(String text, ParsePosition pos, Date date)
	{
		Calendar cal = Calendar.getInstance(Locale.US);

		if (date != null)
			cal.setTime(date);

		String lcase = text.toLowerCase();

		if (lcase.startsWith("today",pos.getIndex()))
		{
			pos.setIndex(pos.getIndex()+5);
			return cal.getTime();
		}
		else if(lcase.startsWith("tomorrow",pos.getIndex()))
		{
			pos.setIndex(pos.getIndex()+8);
			cal.roll(Calendar.DATE,true);
            return cal.getTime();
		}
		else if (lcase.startsWith("yesterday",pos.getIndex()))
		{
			pos.setIndex(pos.getIndex()+9);
			cal.roll(Calendar.DATE,false);
            return cal.getTime();
		}
	
		//December 2010
		// This pattern added by Deepak
		Matcher m = p[0].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
			    // Just set day 1 as no day has been specified.
				int day = 1;
				int month = parseMonth(m.group(1));
				int year = Integer.parseInt(m.group(3));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				currentParseGranularity = Calendar.MONTH;
				return cal.getTime();
			}
			catch (Exception e) {}
		}

		// This pattern added by Deepak
		m = p[1].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
			    // Just set day 1 as no day has been specified.
				int day = 1;
				int month = parseMonth(m.group(1));
				int year = Integer.parseInt(m.group(2));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				currentParseGranularity = Calendar.MONTH;
				return cal.getTime();
			}
			catch (Exception e) {}
		}

		// Existing patterns
		int nextPatternIndex = 2;
		//31st August 1979
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				if (m.group(1) != null)
					parseWeekday(m.group(1));

				int day = Integer.parseInt(m.group(2));
				int month = parseMonth(m.group(3));
				int year = Integer.parseInt(m.group(4));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				return cal.getTime();
			}
			catch (Exception e) {}
		}

		//31st August
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				if (m.group(1) != null)
					parseWeekday(m.group(1));

				int day = Integer.parseInt(m.group(2));
				int month = parseMonth(m.group(3));
				pos.setIndex(m.end());

				cal.set(cal.get(Calendar.YEAR),month,day);
				return cal.getTime();
			}
			catch (Exception e) {}
		}


		//August 31st 1979
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				if (m.group(1) != null)
					parseWeekday(m.group(1));

				int day = Integer.parseInt(m.group(3));
				int month = parseMonth(m.group(2));
				int year = Integer.parseInt(m.group(4));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				return cal.getTime();
			}
			catch (Exception e) {}
		}

		//August 31st
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				if (m.group(1) != null)
					parseWeekday(m.group(1));

				int day = Integer.parseInt(m.group(3));
				int month = parseMonth(m.group(2));
				pos.setIndex(m.end());

				cal.set(cal.get(Calendar.YEAR),month,day);

				return cal.getTime();
			}
			catch (Exception e) {}
		}

		// 08/31/1979
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				int day = Integer.parseInt(m.group(2));
				int month = Integer.parseInt(m.group(1))-1;
				int year = Integer.parseInt(m.group(3));
				pos.setIndex(m.end());

				cal.set(year,month,day);

				return cal.getTime();
			}
			catch (Exception e) {}
		}

		// 1979-08-31
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				int day = Integer.parseInt(m.group(3));
				int month = Integer.parseInt(m.group(2))-1;
				int year = Integer.parseInt(m.group(1));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				return cal.getTime();
			}
			catch (Exception e) {}
		}
		// 5-25-2004 or 12-5-2004 
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				int day = Integer.parseInt(m.group(2));
				int month = Integer.parseInt(m.group(1))-1;
				int year = Integer.parseInt(m.group(3));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				return cal.getTime();
			}
			catch (Exception e) {}
		}
		
		// 5-25-04 or 12-5-04
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				int day = Integer.parseInt(m.group(2));
				int month = Integer.parseInt(m.group(1))-1;
				int year = Integer.parseInt(m.group(3));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				return cal.getTime();
			}
			catch (Exception e) {}
		}
		
		// May or Jun, or June
		//Find the most recent <month>
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			
			try
			{
				int month = parseMonth(text);
				//System.out.println(month);
                if(today==null)
                	 today = new Date();
                //System.out.println("note mon " + today.getMonth());
                if(today.getMonth()<month){
                	 // System.out.println(today.getYear()+1900);
                	cal.set(Calendar.YEAR, today.getYear()+1900-1);
                	cal.set(Calendar.MONTH, month);
                }
                else 
                	{
                	cal.set(Calendar.YEAR, today.getYear()+1900);
                	cal.set(Calendar.MONTH, month);
                	
                	}
                currentParseGranularity = Calendar.MONTH;
				return cal.getTime();
			}
			catch (Exception e) {}
		}
		//Feb-23-2007
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
			   
				int day = Integer.parseInt(m.group(2));
				int month = parseMonth(m.group(1));
				int year = Integer.parseInt(m.group(3));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				return cal.getTime();
			}
			catch (Exception e) {}
		}
		
		//Just 4 digit year 1900-2099
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				int year = Integer.parseInt(text);
				pos.setIndex(m.end());

				cal.set(Calendar.YEAR, year);
				
				currentParseGranularity = Calendar.YEAR;
				
				return cal.getTime();
			}
			catch (Exception e) {}
		}

		// 06/2010
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				int month = Integer.parseInt(m.group(1))-1;
				int year = Integer.parseInt(m.group(2));
				pos.setIndex(m.end());

				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, month);
				
				currentParseGranularity = Calendar.MONTH;
				
				return cal.getTime();
			}
			catch (Exception e) {}
		}
		
		// 06-2010
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
				int month = Integer.parseInt(m.group(1))-1;
				int year = Integer.parseInt(m.group(2));
				pos.setIndex(m.end());

				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, month);
				
				currentParseGranularity = Calendar.MONTH;
				
				return cal.getTime();
			}
			catch (Exception e) {}
		}
		
		//2010
		// This pattern added by Deepak
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
			    // Just set day 1 as no day has been specified.
				int day = 1;
				int month = 1;
				int year = Integer.parseInt(m.group(1));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				currentParseGranularity = Calendar.YEAR;
				return cal.getTime();
			}
			catch (Exception e) {}
		}

		//Month of 2010
		// This pattern added by Deepak
		nextPatternIndex++;
		m = p[nextPatternIndex].matcher(text);

		if (m.find(pos.getIndex()) && m.start()==pos.getIndex())
		{
			try
			{
			    // Just set day 1 as no day has been specified.
				int day = 1;
				int month = parseMonth(m.group(1));;
				int year = Integer.parseInt(m.group(5));
				pos.setIndex(m.end());

				cal.set(year,month,day);
				currentParseGranularity = Calendar.MONTH;
				return cal.getTime();
			}
			catch (Exception e) {}
		}

		return null;
	}

	private static List arrayToList(String[] array)
	{
		List data = new ArrayList();

		for (int i = 0  ; i < array.length ; ++i)
			data.add(array[i].toLowerCase());

		return data;
	}

	private int parseMonth(String month) throws Exception
	{
		String lcm = month.toLowerCase();

		if (months.contains(lcm)) return months.indexOf(lcm);
		if (shortMonths.contains(lcm)) return shortMonths.indexOf(lcm);

		throw new Exception("Invalid Month String");
	}

	private int parseWeekday(String weekday) throws Exception
	{
		String lcw = weekday.toLowerCase();

		if (weekdays.contains(lcw)) return weekdays.indexOf(lcw);
		if (shortWeekdays.contains(lcw)) return shortWeekdays.indexOf(lcw);

		throw new Exception("Invalid Weekday String");
	}

	private int parseEra(String era) throws Exception
	{
		String lce = era.toLowerCase();

		if (eras.contains(lce)) return eras.indexOf(lce);

		throw new Exception ("Invalid Era String");
	}
	
	public String normaltoUSDateString(Date d){
		
		SimpleDateFormat formatter;
        String pattern = "MM-dd-yy ";
        Locale currentLocale = Locale.US;
		formatter = new SimpleDateFormat(pattern, currentLocale);
		
		return formatter.format(d);
		
	}
	
	public Date normaltoUSDate(Date d){
		
		SimpleDateFormat formatter;
        String pattern = "MM-dd-yy ";
        Locale currentLocale = Locale.US;
		formatter = new SimpleDateFormat(pattern, currentLocale);
		Date date = null;
		System.out.println(formatter.format(d));
		try {
			date = formatter.parse(formatter.format(d));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
		
	}
	


	public void setToday(String notedate) {
		today = parse(notedate,new ParsePosition(0));
		System.out.println("note date " + today);
		
	}
}
