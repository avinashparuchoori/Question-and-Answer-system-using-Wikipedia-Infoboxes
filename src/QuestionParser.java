import java.io.*;
import java.util.*;
import java.util.regex.*;


public class QuestionParser {
	public static HashMap<String,String> questionMap = new HashMap<String, String>();
	public static void main(String[] args){
		BufferedReader br;
		String line;
		try{
			br = new BufferedReader(new FileReader("questionlist"));
			while ((line = br.readLine())!= null) {
				HashMap<String, Double> classMap = new HashMap<String, Double>();
				String[] quest = line.split(":");
				String questionKey = findKeyWord(quest[1].toLowerCase());
				String entityKey = quest[0].toLowerCase();
				if(questionMap.containsKey(questionKey)){
					if(questionMap.get(questionKey).contains(entityKey)){
						String val = questionMap.get(questionKey);
						if(val.contains("abbr")){
							val = val.trim();
						}
						Pattern valMatcher = Pattern.compile("^(.*?)\\|\\|"+entityKey+"\\~([0-9\\.]+?)\\|\\|(.*?)$", Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
						String count = valMatcher.matcher(val).replaceAll("$2");
						count = Double.toString(Double.parseDouble(count)+1);
						val = valMatcher.matcher(val).replaceAll("$1\\|\\|"+entityKey+"~"+count+"\\|\\|$3");
						questionMap.put(questionKey, val);
					}
					else{
						String val = questionMap.get(questionKey);
						if(val.contains("abbr~11")){
							val = val.trim();
						}
						val = val + entityKey+"~1||";
						questionMap.put(questionKey, val);
					}
				}
				else{
					String val = "||";
					if(val.contains("abbr~11")){
						val = val.trim();
					}
					val = val + entityKey+"~1||";
					questionMap.put(questionKey, val);
				}
			}
			System.out.println(questionMap);
			for(String key : questionMap.keySet()){
				Double totalcount = (double)0;
				for(String s : questionMap.get(key).split("\\|\\|")){
					if(s.trim().compareTo("")!=0){
						String entityKey = s.split("~")[0];
						String count = s.split("~")[1];
						totalcount = totalcount+ Double.parseDouble(count);
					}
				}
				System.out.println(key +"\t"+ Double.toString(totalcount));
				for(String s : questionMap.get(key).split("\\|\\|")){
					if(s.trim().compareTo("")!=0){
						String entityKey = s.split("~")[0];
						String count = s.split("~")[1];
						count = Double.toString(Double.parseDouble(count)/totalcount);
						String val = questionMap.get(key);
						Pattern valMatcher = Pattern.compile("^(.*?)\\|\\|"+entityKey+"\\~([0-9\\.]+?)\\|\\|(.*?)$", Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);						
						val = valMatcher.matcher(val).replaceAll("$1\\|\\|"+entityKey+"~"+count+"\\|\\|$3");
						questionMap.put(key, val);
					}
				}
			}
			saveHashMapToDisk("questionTypeProbability", questionMap);
		}
		catch(IOException e){
			
		}
	}
	public static String findKeyWord(String s){
		if(s.toLowerCase().contains("what")){
			return "what";
		}
		else if(s.toLowerCase().contains("when")){
			return "when";
		}
		else if(s.toLowerCase().contains("why")){
			return "why";
		}
		else if(s.toLowerCase().contains("which")){
			return "which";
		}
		else if(s.toLowerCase().contains("who")){
			return "who";
		}
		else if(s.toLowerCase().contains("whose")){
			return "whose";
		}
		else if(s.toLowerCase().contains("how")){
			return "how";
		}
		else if(s.toLowerCase().contains("where")){
			return "where";
		}
		else if(s.toLowerCase().contains("whom")){
			return "whom";
		}
		else if(s.toLowerCase().contains("name")){
			return "name";
		}
		else if(s.toLowerCase().contains("give")){
			return "give";
		}
		else if(s.toLowerCase().contains("define")){
			return "define";
		}
		else if(s.toLowerCase().contains("describe")){
			return "describe";
		}
		else if(s.toLowerCase().contains("is")){
			return "is";
		}
		else{
			return s.toLowerCase();
		}
		
	}
	public static <K, V> boolean saveHashMapToDisk(String fileName, HashMap<K, V> map ){
		try{
		Map<K, V> ldapContent = map;
		Properties properties = new Properties();

		for (Map.Entry<K,V> entry : ldapContent.entrySet()) {
		    properties.put(entry.getKey(), entry.getValue().toString());
		}

		properties.store(new FileOutputStream(fileName+".properties"), null);
		return true;
		}
		catch(IOException e){
			return false;
		}
	}
}
