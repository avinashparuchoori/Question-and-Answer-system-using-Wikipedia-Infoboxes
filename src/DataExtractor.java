import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.smu.tspell.*;
import edu.smu.tspell.wordnet.*;
import edu.smu.tspell.wordnet.impl.file.Morphology;
import edu.smu.tspell.wordnet.impl.file.SynsetTypeConverter;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import edu.jhu.nlp.wikipedia.*;

public class DataExtractor {
	static final Pattern infocontentpat = Pattern.compile(
			"\\{\\{ *[A-Za-z]+obox.*?(}}[\n ]*[A-Za-z0-9'\"\\[<])",
			Pattern.DOTALL | Pattern.MULTILINE);
	static final Pattern infoattspat = Pattern
			.compile("\\|([A-Za-z0-9_ ]+?) = (.+)");
	static final Pattern infotypepat = Pattern.compile(
			"\\{\\{ *[Ii]nfobox *([[A-Z][a-z][0-9] ]+?)[\n ]*[\\|<]",
			Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
	static final Pattern catpat = Pattern.compile(
			"\\[\\[Category:([0-9A-Za-z\\.\\*\\(\\)\"' ,-]+)[|]?.*?\\]\\]",
			Pattern.CASE_INSENSITIVE);
	static boolean peoplePage = false;
	static DocumentBuilder docBuilder = null;
	static Document doc = null;
	static Element rootElement = null;
	static Element pageElement = null;
	static int count = 0;
	static HashMap<String, Integer> relationhashmap = new HashMap<String, Integer>();
	static HashSet<String> infohashset = BaseTrees.getInfoBoxSet();
	static int pageCount = 0;
	static String parsedXML = "";
	public static HashMap<String,List<String>> categories = new HashMap<String, List<String>>(); 
	public static HashMap<String,HashMap<String,String>> parsedPages = new HashMap<String, HashMap<String,String>>(); 
	public static stopwordEliminator stopwords = new stopwordEliminator();
	public static HashMap<String, Integer> relationsMap = new HashMap<String, Integer> ();
	public static void main(String[] args) {
		System.setProperty("wordnet.database.dir", "WordNet-3.0"+File.separator+"dict");
		WikiXMLParser wxsp = WikiXMLParserFactory
				.getSAXParser("TestInfoboxDump.xml");
		 //.getSAXParser("/media/Arafath 1TB/Information Retrieval/wiki en xml/enwiki-20131001-pages-articles-multistream.xml");
		long starttime = System.currentTimeMillis();
		try {
			wxsp.setPageCallback(new PageCallbackHandler() {
				public void process(WikiPage page) {
					List<String> categorys = new ArrayList<String>();
					String relation = "";
					try {
						String infotype = "";
						Matcher infotypePatttern = infotypepat.matcher(page
								.getWikiText());
						if (infotypePatttern.find()) {
							infotype = infotypePatttern.group(1).toString()
									.trim();

						}

						if (infotype != "") {
							//if (infohashset.contains(infotype.toLowerCase())) {
								peoplePage = true;
								pageCount++;
								categorys = page.getCategories();
								//do anything with categories here
								categories.put(page.getTitle().trim(), page.getCategories());
								if(page != null && page.getText().length() > 0 && page.getInfoBox() != null){
									parsedPages.put(page.getTitle(), InfoboxParser.
										parseInfoBox(page.getInfoBox().dumpRaw(), page.getTitle(), page.getID(), infotype));
								}
								page.getID();
							//}
						}
					}
					catch(StringIndexOutOfBoundsException e){
						
					}
					catch (DOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println(relation);
					}
					catch(NullPointerException e){
						e.printStackTrace();
						System.out.println(relation);
					}
				}

			});
			wxsp.parse();
			try {
				InfoboxParser.relationsMap = (TreeMap<String,Integer>) sortByValues(InfoboxParser.relationsMap);
				int i =0;
				for(Entry<String, Integer> entry : InfoboxParser.relationsMap.entrySet()){
					if(i>150)
						break;
					relationsMap.put(entry.getKey().trim(), entry.getValue());
					i++;
				}
				File file = new File("InputDataDocumentFormat.xml");
				//File file = new File("TestInputDataDocumentFormat.xml");
				FileWriter fileWriter = new FileWriter(file , true);
				BufferedWriter buffWriter = new BufferedWriter(fileWriter);
				buffWriter.flush();
				buffWriter.write("<add>\n");
				for(String title : parsedPages.keySet()){
					buffWriter.write("<doc>"+"\n");
					boolean flag = false;
					for(String s : parsedPages.get(title).keySet()){
						if(relationsMap.containsKey(s.trim())){
							//<field name="id">F8V7067-APL-KIT</field>
							buffWriter.write("\t<field name=\""+s+"\">"+parsedPages.get(title).get(s).replace("<","").replace(">","")+"</field>"+"\n");
							//buffWriter.write("\t<"+s.trim()+">"+parsedPages.get(title).get(s).replace("<","").replace(">","")+"</"+s.trim()+">\n");
							flag = true;
						}
					}
					buffWriter.write("</doc>"+"\n");
				}
				buffWriter.write("</add>");
				buffWriter.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				File file = new File("InputSchema.xml");
				//File file = new File("TestInputSchema.xml");
				FileWriter fileWriter = new FileWriter(file , true);
				BufferedWriter buffWriter = new BufferedWriter(fileWriter);
				buffWriter.flush();
				for(String s : InfoboxParser.nodes.keySet()){
					if(relationsMap.containsKey(s)){
						buffWriter.write("<field name="+ "\""+ s+"\" "+"type=\""+
					InfoboxParser.nodes.get(s)+"\" indexed=\"true\" stored=\"true\" multiValued=\"false\"/>\n");
					}
				}
				buffWriter.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				//File file = new File("TestInputDataConfig.xml");
				File file = new File("InputDataConfig.xml");
				FileWriter fileWriter = new FileWriter(file , true);
				BufferedWriter buffWriter = new BufferedWriter(fileWriter);
				buffWriter.flush();
				for(String s : InfoboxParser.nodes.keySet()){
					if(relationsMap.containsKey(s.trim())){
						//buffWriter.write("<field column="+ "\""+ s.trim()+"\"      xpath=\"/docs/doc/"+s.trim()+"\">\n");
					}
				}
				buffWriter.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(InfoboxParser.nodes);
		//System.out.println(InfoboxParser.relationsMap);
		saveHashMapToDisk( "relations",relationsMap);
		//saveHashMapToDisk("NameMap", InfoboxParser.nameMap);
		//HashMap<String, Integer> hash = readHashMapFromDisk("WordNet-3.0/dict/relations");
		//expandSynonyms(relationsMap);
		//System.out.println(relationhashmap.size());
		//System.out.println(pageCount);
		//TreeMap<String, Integer> sortedpostingsmap = (TreeMap<String, Integer>) sortByValues(relationhashmap);
		//writeTreetoFile(sortedpostingsmap);
	}

	private static void writeTreetoFile(TreeMap<String, Integer> sortedpostingsmap) {

		File filename = null;
		try {
			String filestr = "";
			TreeMap<String, Integer> toppostings = new TreeMap<String, Integer>();
			int count = 0;
			for (Entry<String, Integer> entry : sortedpostingsmap.entrySet()) {
				if (count < 150) {
					toppostings.put(entry.getKey(), entry.getValue());
					count++;
				} else {
					break;
				}
			}

			filename = new File("relation.data");
			if (!filename.exists()) {
				filename.createNewFile();
			} else {

			}

			FileOutputStream f_out = new FileOutputStream(filename, false);
			BufferedOutputStream buffer_out = new BufferedOutputStream(
					f_out);
			ObjectOutputStream obj_out = new ObjectOutputStream(buffer_out);
			obj_out.writeObject(toppostings);
			// return true;
			obj_out.flush();
			obj_out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("File created at " + filename.toString());


	}

	public static <K, V extends Comparable<V>> Map<K, V> sortByValues(
			final Map<K, V> map) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = map.get(k2).compareTo(map.get(k1));
				// int compare = map.get(k1).compareTo(map.get(k2));
				if (compare == 0)
					return 1;
				else
					return compare;
			}
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}

	private static String processRelation(String str) {

		str = str.trim();
		str = str.replaceAll(" ", "_");

		Pattern catpat = Pattern.compile("[0-9]");

		Matcher mat = catpat.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (mat.find()) {
			mat.appendReplacement(sb, "");
			sb.append(EnglishNumberToWords.convert(Integer.valueOf(mat.group()))
					+ "_");
		}
		mat.appendTail(sb);

		return sb.toString();
	}
	public static boolean expandSynonyms(HashMap<String, Integer> relationMap){
			WordNetDatabase database = WordNetDatabase.getFileInstance();
			HashMap<String, String> synonymHash = new HashMap<String,String>();
			
			for(String s : relationMap.keySet()){
				if(s.split("_").length >= 1){
				//  Display the word forms and definitions for synsets retrieved
				for (String wordForm : s.split("_"))
				{
					Synset[] synsets = database.getSynsets(wordForm);
					//System.out.println("The following synsets contain '" +
						//wordForm + "' or a possible base form " +
						//"of that text:");
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
						}
						//System.out.println(": " + synsets[i].getDefinition());
					}
				}
				}
				
			}
			saveHashMapToDisk("expandedRelations", synonymHash);
			return true;
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
	public static void testing(Object j){
		String s = "";
		s = s.trim();
	}
	public static boolean isStopWord(String s){
		return stopwords.isStopWord(s);
	}
}
