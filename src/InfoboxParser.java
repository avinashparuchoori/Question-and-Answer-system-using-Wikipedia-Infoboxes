

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import DateRule.Month;
import edu.smu.tspell.*;
import edu.smu.tspell.wordnet.*;
import edu.smu.tspell.wordnet.impl.file.Morphology;
import edu.smu.tspell.wordnet.impl.file.SynsetTypeConverter;

public class InfoboxParser {
	static final Pattern releasedatepat = Pattern.compile("\\{\\{([a-zA-Z\\s\\(\\)]+?)\\|([a-zA-Z\\s\\(\\)]+?)\\}\\}", Pattern.CASE_INSENSITIVE);
	static final Pattern releasedatepat2 = Pattern.compile("\\{\\{([a-zA-Z_\\s]+?)?\\|([0-9]{4})\\|?([0-9]{1,})?\\|?([0-9]{1,})?[\\|\\}]{1,}", Pattern.CASE_INSENSITIVE);
	static final Pattern birthdatepat = Pattern.compile("\\{\\{([Bb]irth|[Dd]eath)[\\s_]date([^0-9]+?)?\\|([0-9]{4})\\|([0-9]{1,})\\|([0-9]{1,})\\|?([^\\}]+?)?\\}\\}");
	static final Pattern otherdatespat = Pattern.compile("\\{\\{([a-zA-Z\\s]+?)[\\s_]?date([^0-9]+?)?\\|([0-9]{4})\\|([0-9]{1,})?\\|?([0-9]{1,})?\\|?([^\\}]+?)?\\}\\}", Pattern.CASE_INSENSITIVE);
	static final Pattern linkpat = Pattern.compile("\\[{2}(([^\\|\\]]+?)\\|)?([^\\|\\]]+?)\\]{2}", Pattern.MULTILINE + Pattern.DOTALL);
	static final Pattern speciallinkpat = Pattern.compile("\\[{2}(([^\\,\\(|]+?)((,|\\()[^\\|\\#]+?))\\|\\]{2}", Pattern.DOTALL);
	static final Pattern namespacelinkpat = Pattern.compile("\\[{2}(([^\\,\\(|]+?):([^\\|\\#]+?))((,|\\()([^\\|\\#]+?))?\\|\\]{2}", Pattern.DOTALL);
	static final Pattern externallinkpat = Pattern.compile("\\[htt[p|ps]:([^\\s\\]]+?)(\\s([^\\]]+?))?\\]",Pattern.CASE_INSENSITIVE);
	static final Pattern linkmarkuppat = Pattern.compile("\\[\\[([^\\|]+?)\\|?\\]\\]", Pattern.DOTALL + Pattern.MULTILINE);
	static final Pattern filepat = Pattern.compile("\\[\\[(\\s)?(Image|ftp|gopher|telnet|file|notes|ms-help|media)(:)(.+?)(\\|([^\\|]+?))?\\]\\]",Pattern.CASE_INSENSITIVE);
	static final Pattern textformatpat1 = Pattern.compile("\\{\\{([^\\|\\}]+?)\\|([^\\|\\}]+?)[\\|\\}]([^\\}]+?)?\\}{1,}");
	static final Pattern textformatpat2 = Pattern.compile("\\{(.*)\\}", Pattern.MULTILINE + Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
	static final Pattern textformatpat3 = Pattern.compile("<!--([^<>]+?)-->", Pattern.MULTILINE + Pattern.DOTALL);
	static HashMap<String,String> nodes = new HashMap<String,String>();
	static TreeMap<String,Integer> relationsMap = new TreeMap<String,Integer>();
	//static HashMap<String,Integer> nameMap = new HashMap<String,Integer>();
public static void main(String[] args) throws IOException{
	HashMap <String,String> tokens = new HashMap<String,String>();
	String filename="writtenStuff.txt";
	String line = "";
	String rawInfobox = "";
	BufferedReader br;
	try{
		br = new BufferedReader(new FileReader(filename));
		while ((line = br.readLine())!= null) {
			rawInfobox = rawInfobox + "\n"+ line.trim();
		}
		tokens = parseInfoBox(rawInfobox,"1","1", "testtype");
		for(String s : tokens.keySet()){	
			//<field name="id">F8V7067-APL-KIT</field>
			System.out.println("<"+s.trim()+">"+" "+tokens.get(s).trim()+" "+"</"+s.trim()+">");
		}
		System.out.println(nodes.keySet());
		for (String s : nodes.keySet()){
			if(nodes.get(s).trim().compareTo("")!= 0)
			s = "<field name="+ "\""+ s+"\" "+"type=\""+nodes.get(s)+"\" indexed=\"true\" stored=\"true\" multiValued=\"false\"/>";
			else
			s = "<field name="+ "\""+ s+"\" "+"type=\"text_general\" indexed=\"true\" stored=\"true\" multiValued=\"false\"/>";
			System.out.println(s);
		}
	}
	catch(Exception e){
		String s = "";
		s = s+"";
	}
}
public static HashMap<String,String> parseInfoBox(String rawText, String title, String id, String infotype){
	HashMap <String,String> tokens = new HashMap<String,String>();
	Pattern splitpat = Pattern.compile("\\|([A-Za-z_\\s]+?)=(.+?)?$",Pattern.MULTILINE+Pattern.CASE_INSENSITIVE);
	rawText = splitpat.matcher(rawText).replaceAll("\\|\\|\\|$1 = $2");
	rawText = rawText.replaceAll("<ref([^<]+?)?>(.+?)</ref>","");
	rawText = textformatpat3.matcher(rawText).replaceAll("");
	String [] linesInfobox = rawText.split("\\|\\|\\|");
	if(rawText.contains("Vendetta")){
		rawText = rawText.trim();
	}
	for(String s : linesInfobox){
		s = s.toLowerCase().trim();
		if(!((s.compareTo("") == 0 ) || s.contains("{{infobox") || s.trim().replace("}}", "").length() < 2)){
			s = s.replace("\n"," ");
			s = s.replaceAll("<br.>", ",");
			s = parseTagFormatting(s);
			s = parseTemplates(s);
			s = parseLinks(s);
			if(!(s.contains("_date") || s.toLowerCase().contains("date") || s.contains("formation"))){
				s = textformatpat1.matcher(s).replaceAll("$2");
				s = textformatpat2.matcher(s).replaceAll("");
				if(s.contains("}}"))
				{
				Pattern endpat = Pattern.compile("([^\\}]+?)\\}{2,}$",Pattern.MULTILINE+Pattern.DOTALL);
				s = endpat.matcher(s).replaceAll("$1").trim();
				}
			}
			String[] data;
				s = s.replace("&"," and ");
			if(s.matches("\\s?([a-zA-Z0-9_]+?)=([^\\=^\\}\\}]+?)")){
				data = s.split("=");
			}
			else if(s.matches("\\s?([a-zA-Z0-9_\\s]+?)=(.+?)")){
				data = new String[2];
				data[0] = Pattern.compile("\\s?([a-zA-Z0-9_\\s]+?)=(.+?)$",Pattern.MULTILINE).matcher(s).replaceAll("$1");
				data[1] = s.replaceAll("\\s?([a-zA-Z0-9_\\s]+?)=(.+?)", "$2");
			}
			else if(s.matches("([a-zA-Z0-9_]+?)=")){
				data = s.split("=");
			}
			else{
				data = s.split("\\s=\\s?");
			}
			data[0] = data[0].trim();
			if(data.length > 1)
				data[1]= data[1].trim();

			if(data[0].compareTo("birth_date") == 0 || data[0].compareTo("death_date") == 0){
					String month = "";
					String day = "";
					if(data.length > 1 && data[1].compareTo("") != 0){
					data[1] = new DateRule().apply(data[1]);
					Matcher birthdateMatch = birthdatepat.matcher(data[1]);
					data[0] = data[0];
					if(birthdateMatch.matches()){
						if(birthdateMatch.replaceAll("$4").length() <= 1){
							month = "0"+ birthdateMatch.replaceAll("$4");
						}
						else
						{
							month = birthdateMatch.replaceAll("$4");
						}
						if(birthdateMatch.replaceAll("$5").length() <= 1){
							day = "0"+ birthdateMatch.replaceAll("$5");
						}
						else
						{
							day = birthdateMatch.replaceAll("$5");
						}
						data[1] = birthdateMatch.replaceAll("$3")+month+day;
						}
					}
				}
				if(data[0].contains("_date") || data[0].contains("released") || data[0].contains("formation")){
					String month = "";
					String day = "";
					if(data.length > 1 && data[1].compareTo("")!=0){
						data[1] = new DateRule().apply(data[1]);
						Matcher otherdatesMatch = otherdatespat.matcher(data[1]);
						if(otherdatesMatch.matches()){
							if(otherdatesMatch.groupCount() > 4 && otherdatesMatch.group(4) != null && otherdatesMatch.group(4).trim().compareTo("") != 0 ){
								if(otherdatesMatch.replaceAll("$4").length() <=1)
								 month = "0"+otherdatesMatch.replaceAll("$4");
								else
									month = otherdatesMatch.replaceAll("$4");
							}
							otherdatesMatch = otherdatespat.matcher(data[1]);
							if(otherdatesMatch.groupCount() > 5 && otherdatesMatch.replaceAll("$5").trim().compareTo("") != 0 ){
								if(otherdatesMatch.replaceAll("$5").length() <=1)
								 day = "0"+otherdatesMatch.replaceAll("$5");
								else
									day = otherdatesMatch.replaceAll("$5");
							}
							data[1] = otherdatesMatch.replaceAll("$3")+month+day;
						}
					}
				}
			data[0] = formatXMLTag(data[0]);
			if(data.length >1){
				if(data[0].compareTo("") != 0){
					if(data[0].equalsIgnoreCase("years_active") || data[0].equalsIgnoreCase("yearsactive"))
						tokens.put(data[0], formatText(yearsFormatText(data[1])));
					else if(data[0].contains("release"))
						tokens.put(data[0], formatText(parseDates(releaseFormatText(data[1].trim()))));
					else if(data[0].contains("date") || data[0].contains("formation") || data[0].contains("extinction"))
						tokens.put(data[0],parseDates(data[1]));
					else
						tokens.put(data[0],formatText(data[1])); 
					
				}
			}
			else if(data[0].compareTo("") != 0)
				tokens.put(data[0],""); 	
			if(data[0].compareTo("")!= 0 && !nodes.containsKey(data[0])){
				if(data[0].contains("_date"))
						nodes.put(data[0], "text_general_custom");
				else
						nodes.put(data[0], "text_general_custom");
				}
			if(data[0].compareTo("")!= 0 && !relationsMap.containsKey(data[0])){
					relationsMap.put(data[0], 1);
			}
			else
			{
				if(data[0].compareTo("")!= 0)
					relationsMap.put(data[0],relationsMap.get(data[0])+1);
			}
			
			}
		//}
			
	}
	nodes.put("title", "text_general");
	nodes.put("id", "text_general");
	nodes.put("infotype", "text_general");
	nodes.put("entitytype", "text_general");
	nodes.put("link", "text_general");
	if(relationsMap.containsKey("title"))
		relationsMap.put("title", relationsMap.get("title")+1);
	else
		relationsMap.put("title", 1);
	if(relationsMap.containsKey("id"))
		relationsMap.put("id", relationsMap.get("id")+1);
	else
		relationsMap.put("id", 1);
	if(relationsMap.containsKey("infotype"))
		relationsMap.put("infotype", relationsMap.get("infotype")+1);
	else
		relationsMap.put("infotype", 1);
	if(relationsMap.containsKey("entitytype"))
		relationsMap.put("entitytype", relationsMap.get("entitytype")+1);
	else
		relationsMap.put("entitytype", 1);
	if(relationsMap.containsKey("link"))
		relationsMap.put("link", relationsMap.get("link")+1);
	else
		relationsMap.put("link", 1);
	//nameMap.put(formatText(title).toLowerCase(), 1);
	infotype = infotype.trim();
	title = title.trim().replace("&", "~and~");
	tokens.put("id", id.trim());
	tokens.put("title", formatText(title));
	tokens.put("infotype", infotype);
	title = "http://en.wikipedia.org/wiki/"+title.replace(" ", "_");
	tokens.put("link", title);
	if(infotype.contains("musical"))
		tokens.put("entitytype", "hum");
	else if(infotype.contains("film"))
		tokens.put("entitytype", "enty");
	else if(infotype.contains("organization"))
		tokens.put("entitytype", "enty");
	return tokens;
}
public static String releaseFormatText(String s){
	Matcher releasematch = releasedatepat.matcher(s);
	if(releasematch.find()){
		s = releasematch.replaceAll("$2").replace("(", "").replace(")","");
	}
	Matcher releasedatematch = releasedatepat2.matcher(s);
	if(releasedatematch.find()){
		s = releasedatematch.replaceAll("$2$3$4").replaceAll("[\\|\\s]y[\\|\\s\\}]" , "").replace("|", " ").replace("}", "");
	}
	Pattern release = Pattern.compile("([0-9]{4})([0-9]{1,2})([0-9]{1,2})");
	Matcher datematch = release.matcher(s);
	while(datematch.find()){
		if(datematch.group(2).length() == 1 && datematch.group(3).length() == 1){
			s = s.replace(datematch.group(0), datematch.group(1)+"0"+datematch.group(2)+"0"+datematch.group(3));
		}
		else if(datematch.group(2).length() > 1 && datematch.group(3).length() == 1){
			if(Integer.parseInt(datematch.group(2)) > 12){
				s = s.replace(datematch.group(0), datematch.group(1)+"0"+datematch.group(2)+datematch.group(3));
			}
			else{
				s = s.replace(datematch.group(0), datematch.group(1)+datematch.group(2)+"0"+datematch.group(3));
			}
		}
	}
	return s;
}
public static String parseDates(String s){
	s = s.trim();
	Pattern yearPat = Pattern.compile("([0-9]{4})0101");
	Pattern datePat = Pattern.compile("([0-9]{4})([0-9]{2})([0-9]{2})");
	if(yearPat.matcher(s).find())
		return yearPat.matcher(s).replaceAll("$1");
	Matcher dateMatcher = datePat.matcher(s);
	while(dateMatcher.find()){
		String actual = dateMatcher.group(0);
		String month = new DateRule().getMonth(dateMatcher.group(2));
		String year = dateMatcher.group(1);
		String day = dateMatcher.group(3);
		s = s.replace(actual, day+" "+month+" "+year);
	}
	s = s.replaceAll("[Dd]ate", "");
	s = s.replaceAll("[\\{\\}]", "");
	s = s.replaceAll("\\|"," ");
	s = s.replaceAll("[Bb]irth", "");
	s = s.replaceAll("[Dd]eath", "");
	s = s.replaceAll("[Ss]tart", "");
	return s;
}
public static String yearsFormatText(String s){
	s = s.toLowerCase();
	s = s.replace("–", " - ");
	s = s.replaceAll("[^0-9A-Za-z\\-\\.\\s_]", "");
	s = s.replaceAll("([0-9]{4})present", "$1"+" - "+"present");
	s = s.replaceAll("([0-9]{4})([0-9]{4})", "$1"+" - "+"$2");
	return s;
}
public static String formatText(String s){
	s = s.replaceAll("\\*{1,}", ",");
	Pattern QuotePat = Pattern.compile("[\"']{1,}([a-zA-Z_ ]+?)[\"']{1,}");
	if(QuotePat.matcher(s).matches())
		s = QuotePat.matcher(s).replaceAll("$1");
	s = new SpecialCharRule().apply(s);
	s = new AccentRule().apply(s);
	return s;
}
public static String formatXMLTag(String s){
	/*producer // producers production_company production
	instrument //instruments
	birth_date // born
	death_date //died death
	past_members // former_members
	writer // writers
	label //labels
	preceeded_by //predecessor
	alias //also_known_as
	formation // formed */
	s = s.trim();
	s = s.replaceAll("\\s([a-zA-Z0-9])", "_$1").replace("/", "_").replaceAll("^([0-9](.+?))", "num_$1");
	s = new SpecialCharRule().apply(s);
	if(s.contains("release"))
		s = "released";
	else if(s.equals("sprouse"))
		s = "spouse";
	else if(s.equals("mcaption"))
		s = "caption";
	else if(s.equals("producers") || s.equals("production_company") || s.equals("production"))
		s = "producer";
	else if(s.equals("isntruments"))
		s = "instrument";
	else if(s.equals("born"))
		s = "birth_date";
	else if(s.equals("died") || s.equals("death"))
		s = "death_date";
	else if(s.equals("former_members"))
		s = "past_members";
	else if(s.equals("writers"))
		s = "writer";
	else if(s.equals("labels"))
		s = "label";
	else if(s.equals("predecessor"))
		s = "preceeded_by";
	else if(s.equals("also_known_as"))
		s = "alias";
	else if(s.equals("formed"))
		s = "formation";
	return s;
}
public static String formatDateTime(String s){
	if(s.matches("[0-9]{4}\\s?–"))
		s = s + Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
	return s;
}
public static String parseTagFormatting(String text) {
	text = text.replaceAll("&([a-zA-Z0-9]+?);", " ");
	text = text.replaceAll("&amp;", "");
	Pattern tagPat = Pattern.compile("(\\s<([^<>]+?)>|<([^<>]+?)>)",Pattern.MULTILINE + Pattern.DOTALL);
	Pattern tagPat1 = Pattern.compile("(&lt;(.+?)&gt;)",Pattern.MULTILINE + Pattern.DOTALL);
	Pattern tagPat2 = Pattern.compile("&quot",Pattern.MULTILINE + Pattern.DOTALL);
	Pattern tagPat3 = Pattern.compile("&ndash;",Pattern.MULTILINE + Pattern.DOTALL);
	String itemtext = "";
	if (text != null)
		itemtext = text;
	Matcher tagMatch = tagPat.matcher(itemtext);
	Matcher tag1Match = tagPat1.matcher(tagMatch.replaceAll("").trim());
	Matcher tag2Match = tagPat2.matcher(tag1Match.replaceAll("").trim());
	Matcher tag3Match = tagPat3.matcher(tag2Match.replaceAll("").trim());
	if(text == null)
		return null;
	else
		return tag3Match.replaceAll("").trim();
}
public static String parseTemplates(String text) {
	Pattern tempPat = Pattern.compile("\\{\\{([^\\{\\{]+?)\\}\\}", Pattern.DOTALL + Pattern.MULTILINE);
	String itemtext = "";
	if (text != null)
		itemtext = text;
	Matcher tempMatch = tempPat.matcher(itemtext);
	if (text == null)
		return null;
	else if(tempMatch.matches())
		return tempMatch.replaceAll("$1").trim();
	else 
		return text;
}
public static String parseLinks(String text) {
	String[] links = {"",""};
	String displayLink = "", actualLink = "";
	String itemtext = "";
	if (text != null)
		itemtext = text;
	Matcher fileMatch = filepat.matcher(itemtext);
	if(fileMatch.find()){
			itemtext = fileMatch.replaceAll("$6");
			links[0] = itemtext.trim();
	}
	Matcher linkMatch = linkpat.matcher(itemtext);
	while(linkMatch.find()) {			
		if(linkMatch.group(2) == null) {
			actualLink = linkMatch.group(3).replace(" ","_");
			char[] stringarray = actualLink.toCharArray();
			stringarray[0] = Character.toUpperCase(stringarray[0]);
			actualLink = new String(stringarray);
		} else {
			actualLink = linkMatch.group(2).replace(" ", "_");
			char[] stringarray = actualLink.toCharArray();
			stringarray[0] = Character.toUpperCase(stringarray[0]);
			actualLink = new String(stringarray);
		}
		if(actualLink.indexOf(":") < 0){
			links[1] = actualLink;
		}
	}
	itemtext = linkMatch.replaceAll("$3");
	itemtext = itemtext.replace("<nowiki />", "");
	links[0] = itemtext;
	Matcher namespacelinkMatch = namespacelinkpat.matcher(itemtext);
	if(namespacelinkMatch.find()){
		itemtext = namespacelinkMatch.replaceAll("$3");
		links[0] = itemtext.trim();
	}
	Matcher speciallinkMatch = speciallinkpat.matcher(itemtext);
	if(speciallinkMatch.find())
	{
		itemtext = speciallinkMatch.replaceAll("$2").trim();
		actualLink = speciallinkMatch.replaceAll("$1").replace(" ", "_");
		char[] stringarray = actualLink.toCharArray();
		stringarray[0] = Character.toUpperCase(stringarray[0]);
		actualLink = new String(stringarray);
		links[1] = actualLink;
		links[0] = itemtext;
	
	}
	Matcher linkmarkupMatch = linkmarkuppat.matcher(itemtext);
	if(linkmarkupMatch.find())
	{
		itemtext = linkmarkupMatch.replaceAll("$1");
		links[0] = itemtext;
	}
	if (text == null)
		return links[0];
	else
		return links[0];		
}
}
