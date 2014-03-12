import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.*;
import java.util.Properties;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordSense;


public class expandRelations {
	public static stopwordEliminator stopwords = new stopwordEliminator();
	public static void main(String[] args){
		System.setProperty("wordnet.database.dir", "WordNet-3.0"+File.separator+"dict");
		HashMap<String,Integer> hashRelations = new HashMap<String,Integer>();
		for(Entry s : readHashMapFromDisk("relations").entrySet()){
			hashRelations.put(s.getKey().toString(), Integer.parseInt(s.getValue().toString()));
		}
		expandSynonyms(hashRelations);
	}
	public static boolean expandSynonyms(HashMap<String, Integer> relationMap){
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		HashMap<String, String> synonymHash = new HashMap<String,String>();
		
		for(String s : relationMap.keySet()){
			if(s.split("_").length >= 1){
			//  Display the word forms and definitions for synsets retrieved
			synonymHash.put(s.trim(), "~~"+s.trim());
			for (String wordForm : s.split("_"))
			{
				if(s.contains("birth")){
					s = s.trim();
				}
				Synset[] synsets = database.getSynsets(wordForm);
				for (int i = 0; i < synsets.length; i++)
				{
					//System.out.println("");
					List<String> wordForms = new ArrayList<String>();
					
					wordForms = new ArrayList<String>(Arrays.asList((synsets[i].getWordForms())));					
					for(WordSense alterMatch : synsets[i].getDerivationallyRelatedForms(wordForm)){
						for(String match : alterMatch.getWordForm().split(" ")){
							if( !isStopWord(match) && !wordForms.contains(match)){
								wordForms.add(match);
							}
						}
						
					}
					for (int j = 0; j < wordForms.size(); j++)
					{
						s = s.toLowerCase();
						String key = wordForms.get(j);
						key = key.toLowerCase();
						String value = "";
						key = key.replace(" ", "_");
						if(synonymHash.containsKey(key)){
							value = synonymHash.get(key);
							if(!(value.contains("~"+s+"~") || value.contains("="+s+"~") || value.contains("~"+s))){
								synonymHash.put(key, value+"~~"+s);	
							}
						}
						else{
							synonymHash.put(key, "~~"+s);
						}
						//System.out.println(key);
					}
					if(!s.contains("_")){
						if(!synonymHash.containsKey(s.toLowerCase())){
							synonymHash.put(s.trim().toLowerCase(),"~~s");
						}
					}
					//System.out.println(": " + synsets[i].getDefinition());
				}
			}
			}
			
		}
		saveHashMapToDisk("expandedRelations", synonymHash);
		return true;
	}
	public static boolean isStopWord(String s){
		return stopwords.isStopWord(s);
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
	public static <K, V> HashMap<K ,V> readHashMapFromDisk(String fileName){
		Map<K, V> ldapContent = new HashMap<K, V>();
		Properties properties = new Properties();
		try{
			properties.load(new FileInputStream(fileName+".properties"));
		for (Object key : properties.keySet()) {
		   ldapContent.put((K)key, (V)properties.get(key));
		}
			return (HashMap<K, V>) ldapContent;
		}
		catch(IOException e){
			return (HashMap<K, V>) ldapContent;
		}
	}
	
}
