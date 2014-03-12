import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class DateRule {

	public String apply(String token) {
		// TODO Auto-generated method stub
		int current, prev, next, month, date, time;
		StringBuffer sb = new StringBuffer();
		int index;
	//	if(stream != null) {
		//	String token;
			String tokenUnchanged;
			try{
			if(1 == 1) { 
					//token = stream.next();
					tokenUnchanged = token;
					Pattern ddmmyyyyPat = Pattern.compile("(\\d{1,2})\\s([^\\s]+?)\\s([\\d]{4})");
					Matcher ddmmyyyyMatch = ddmmyyyyPat.matcher(token);
					boolean ddmmyyyy = false;
					while(ddmmyyyyMatch.find()){
						String match = ddmmyyyyMatch.group(2);
						if(Month.parse(match) > -1){
							String datepart = ddmmyyyyMatch.group(1);
							if(datepart.length() < 2)
								datepart = "0"+datepart;
							ddmmyyyyMatch.appendReplacement(sb, ddmmyyyyMatch.group(3) + monthMatcher(match)+ datepart);
							ddmmyyyy = true;
						}
					}
					if(ddmmyyyy)
					token = ddmmyyyyMatch.appendTail(sb).toString();
					
					Pattern mmddyyyyPat = Pattern.compile("([^\\s]+?)\\s(\\d{1,2}),\\s([\\d]{4})");
					Matcher mmddyyyyMatch = mmddyyyyPat.matcher(token);
					boolean mmddyyyy = false;
					sb = new StringBuffer();
					while(mmddyyyyMatch.find()){
						String match = mmddyyyyMatch.group(1);
						if(Month.parse(match) > -1){
							String datepart = mmddyyyyMatch.group(2);
							if(datepart.length() < 2)
								datepart = "0"+datepart;
							mmddyyyyMatch.appendReplacement(sb, mmddyyyyMatch.group(3) + monthMatcher(match)+ datepart);
							mmddyyyy = true;
						}
					}
					if(mmddyyyy)
					token = mmddyyyyMatch.appendTail(sb).toString();

					Pattern yyyBCPat = Pattern.compile("([\\d]{2,4})\\s(BC|AD)", Pattern.CASE_INSENSITIVE);
					Matcher yyyBCMatch = yyyBCPat.matcher(token);
					boolean yyyBC = false;
					sb = new StringBuffer();
					while(yyyBCMatch.find()){
						int yr = Integer.parseInt(yyyBCMatch.group(1));
						String match = new DecimalFormat("0000").format(yr);
						if(yyyBCMatch.group(2).trim().toUpperCase().compareTo("BC") == 0)
							match = "-" + match;
						yyyBCMatch.appendReplacement(sb, match+"0101");
						yyyBC = true;
					}
					if(yyyBC)
						token = yyyBCMatch.appendTail(sb).toString();
					
					Pattern timePat = Pattern.compile("([\\d]{1,2}):([\\d]{1,2})\\s?(AM|PM)", Pattern.CASE_INSENSITIVE);
					Matcher timeMatch = timePat.matcher(token);
					boolean timeBool = false;
					sb = new StringBuffer();
					while(timeMatch.find()){
						int hrs = Integer.parseInt(timeMatch.group(1).trim());
						if(timeMatch.group(3).trim().toUpperCase().compareTo("PM") == 0){
							hrs = hrs+12;
						}
						String hrstring = new DecimalFormat("00").format(hrs);
						timeMatch.appendReplacement(sb, hrs+":"+timeMatch.group(2).trim()+":00");
						timeBool = true;
					}
					if(timeBool)
						token = timeMatch.appendTail(sb).toString();
					
					Pattern yyyyPat = Pattern.compile("((\\s|\\.|\\,|\\(|\\)|^)([\\d]{4})(\\s|\\.|\\,|\\(|\\)|$))", Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
					Matcher yyyyMatch = yyyyPat.matcher(token);
					boolean yyyy = false;
					sb = new StringBuffer();
					while(yyyyMatch.find()){
						int yr = Integer.parseInt(yyyyMatch.group(3).trim());
						String match = new DecimalFormat("0000").format(yr);
						yyyyMatch.appendReplacement(sb, " "+match+"0101" +" ");
						yyyy = true;
					}
					if(yyyy)
						token = yyyyMatch.appendTail(sb).toString();
					
					Pattern totalTimePat = Pattern.compile("([\\d]{1,2}:[\\d]{1,2}:[\\d]{1,})\\s?(UTC)?\\s?([^\\s]+?)?\\s?([^\\s]+?)?\\s([\\d]{8})");
					Matcher totalTimeMatch = totalTimePat.matcher(token);
					boolean totalTime = false;
					sb = new StringBuffer();
					while(totalTimeMatch.find()){
						String match = totalTimeMatch.group(5).trim()+ " " + totalTimeMatch.group(1);
						totalTimeMatch.appendReplacement(sb, match);
						totalTime = true;
					}
					if(totalTime)
						token = totalTimeMatch.appendTail(sb).toString();
					
					Pattern onlyMonthPat = Pattern.compile("([A-Za-z]+?)\\s([\\d]{1,2})(\\s|\\.|\\,|$)");
					Matcher onlyMonthMatch = onlyMonthPat.matcher(token);
					boolean onlyMonth = false;
					sb = new StringBuffer();
					while(onlyMonthMatch.find()){
						String match = onlyMonthMatch.group(1).trim();
						if(Month.parse(match) > -1){
							int dt = Integer.parseInt(onlyMonthMatch.group(2).trim());
							String dttime = new DecimalFormat("00").format(dt);
							onlyMonthMatch.appendReplacement(sb, "1900"+monthMatcher(match)+ dttime+ " ");
							onlyMonth = true;
						}
						
					}
					if(onlyMonth)
						token = onlyMonthMatch.appendTail(sb).toString();
					
					Pattern mulYearsPat = Pattern.compile("([\\d]{4})([^\\d\\w\\s\\p{Punct}])(\\d{2})");
					Matcher mulYearsMatch = mulYearsPat.matcher(token);
					boolean mulYears = false;
					sb = new StringBuffer();
					while(mulYearsMatch.find()){
						String year1 = mulYearsMatch.group(1).trim();
						String year2 = mulYearsMatch.group(1).trim().substring(0,2)+mulYearsMatch.group(3).trim();
						mulYearsMatch.appendReplacement(sb,year1+"0101"+mulYearsMatch.group(2).trim()+year2+"0101");
						mulYears = true;
					}
					if(mulYears)
						token = mulYearsMatch.appendTail(sb).toString();
					
					if(token.compareTo(tokenUnchanged) != 0){
					//	stream.previous();
						//stream.set(token);
						//stream.next();
					}
				}
			//stream.reset();
			}
	catch(IndexOutOfBoundsException e){
			String s = "ajahsjdh";	
			}
			return token;
	}
	public String getMonth(String s){
		return Month.getMonth(s.trim());
	}
	private int isDateCharOnly(String token) {
		return token.indexOf("[0-9\\:�]*");
	}
	private String monthMatcher(String token){
		int month = Month.parse(token);
		return new DecimalFormat("00").format(month);
	}
	
	public enum Month {
		  JANUARY(1), FEBRUARY(2), MARCH(3),
		  APRIL(4),   MAY(5),      JUNE(6),
		  JULY(7),    AUGUST(8),   SEPTEMBER(9),
		  OCTOBER(10),NOVEMBER(11),DECEMBER(12);
		  
		  public int index;
		  public static int parse(String s) {
			    s = s.trim();
			    for (Month m : Month.values())
			      if (m.matches(s.trim()))
			        return m.index;
			    return -1;
			  }
		  public static String getMonth(String s){
			  int index = Integer.parseInt(s);
			  for(Month m : Month.values()){
				  if(m.index == index){
					  return m.toString().toLowerCase();
				  }
			  }
			  return s;
		  }

			  private boolean matches(String s) {
			    return s.equalsIgnoreCase(toString());
			  }

			  Month(int index) {
			    this.index = index;
			  }
	}


}