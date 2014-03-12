import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordSense;


public class relationDocCreator {
	public static HashMap<String,String> expandedRelations = new HashMap<String,String>();
	public static HashMap<String,String> fieldRelations = new HashMap<String, String>();
	public static HashMap<String, String> entityTypes = new HashMap<String,String>();
	public static HashMap<String, String> relationsProperties = new HashMap<String, String>();
	public static stopwordEliminator stopwords = new stopwordEliminator();
	public static void main(String[] args){
		System.setProperty("wordnet.database.dir", "WordNet-3.0"+File.separator+"dict");
		LoadRelationsData("expandedRelations");
		LoadEntitiesData("Mappings to top 150 fields");
		LoadRelationsProperties("relations");
		CreateIndexFile("IndexFileName.xml");
		for(String key : relationsProperties.keySet()){
			if(!fieldRelations.containsKey(key)){
				System.out.println(key);
			}
		}
	}
	public static void CreateIndexFile(String indexFileName){
	try{
		File file = new File(indexFileName);
		//File file = new File("TestInputDataDocumentFormat.xml");
		FileWriter fileWriter = new FileWriter(file , true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		buffWriter.flush();
		buffWriter.write("<add>\n");
		int count = 0;
		for(Entry relation : relationsProperties.entrySet()){
			String key = relation.getKey().toString().trim();
			String value = relation.getValue().toString().trim();
			boolean flag = false;
			if(entityTypes.containsKey(key) &&relationsProperties.containsKey(key)){
				buffWriter.write("<doc>"+"\n");
					count++;
					//<field name="id">F8V7067-APL-KIT</field>
					buffWriter.write("\t<field name=\"id\">"+Integer.toString(count)+"</field>"+"\n");
					buffWriter.write("\t<field name=\"fieldid\">"+key+"</field>"+"\n");
					buffWriter.write("\t<field name=\"relations\">"+expandRelations(key)+"</field>"+"\n");
					buffWriter.write("\t<field name=\"entity\">"+entityTypes.get(key)+"</field>"+"\n");
					buffWriter.write("\t<field name=\"count\">"+relationsProperties.get(key)+"</field>"+"\n");
					buffWriter.write("</doc>"+"\n");	
			}
			else{
				//System.out.println(key);
			}
		}
		buffWriter.write("</add>");
		buffWriter.close();
		System.out.println(Integer.toString(count));
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	public static void LoadRelationsProperties(String propertiesFile){
		HashMap<Object,Object> entities = new HashMap<Object,Object>();
		entities = readHashMapFromDisk(propertiesFile);
		for(Entry entity : entities.entrySet()){
			String key = entity.getKey().toString().trim();
			String value = entity.getValue().toString().trim().toLowerCase();
			relationsProperties.put(key, value);
		}
	}
	public static void LoadRelationsData(String relationsfile){
		HashMap<Object,Object> relations = new HashMap<Object,Object>();
		relations = readHashMapFromDisk(relationsfile);
		for(Entry relation :  relations.entrySet()){
			String key = relation.getKey().toString();
			String values = relation.getValue().toString();
			for(String s : values.split("~~")){
				if(s.trim().compareTo("") != 0){
					s = s.trim();
					if(fieldRelations.containsKey(s)){
						String value = fieldRelations.get(s);
						fieldRelations.put(s, value + " "+key.replace("_", " "));
					}
					else{
						fieldRelations.put(s," "+key.replace("_", " "));
					}
				}
			}
		}
	}
	public static String expandRelations(String s){
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		String forms = " ";
		if(s.split("_").length >= 1){
			//  Display the word forms and definitions for synsets retrieved
			for (String wordForm : s.split("_"))
			{
				Synset[] synsets = database.getSynsets(wordForm);
				for (int i = 0; i < synsets.length; i++)
				{
					//System.out.println("");
					List<String> wordForms = new ArrayList<String>();
					
					wordForms = new ArrayList<String>(Arrays.asList((synsets[i].getWordForms())));	
					for(String wordmatch : wordForms){
						forms = forms + wordmatch+" ";
					}
					for(WordSense alterMatch : synsets[i].getDerivationallyRelatedForms(wordForm)){
						for(String match : alterMatch.getWordForm().split(" ")){
							if( !isStopWord(match)){
								forms = forms + match+" ";
							}
						}						
					
					}
					
					//System.out.println(": " + synsets[i].getDefinition());
				}
			}
			}
		return forms.trim();
	}
	public static void LoadEntitiesData(String entitiesfile){
		HashMap<Object,Object> entities = new HashMap<Object,Object>();
		entities = readHashMapFromDisk(entitiesfile);
		for(Entry entity : entities.entrySet()){
			String key = entity.getKey().toString().trim();
			String value = entity.getValue().toString().trim().toLowerCase();
			entityTypes.put(key, value);
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
	public static boolean isStopWord(String s){
		return stopwords.isStopWord(s);
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
