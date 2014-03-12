import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXParseException;

import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

public class Extract_InfoBox_Categories {
	public static HashMap<String,HashMap<String,String>> parsedPages = new HashMap<String, HashMap<String,String>>(); 
	public static StringBuilder sb = new StringBuilder();
	public static void main(String[] args) {
		System.setProperty("wordnet.database.dir", "WordNet-3.0/dict");
		WikiXMLParser wxsp = WikiXMLParserFactory
				// .getSAXParser("WikiDump_1600.xml");
		 .getSAXParser("/media/Arafath 1TB/Information Retrieval/wiki en xml/enwiki-20131001-pages-articles-multistream.xml");
		long starttime = System.currentTimeMillis();
		try {
			wxsp.setPageCallback(new PageCallbackHandler() {
				public void process(WikiPage page) {
					List<String> categorys = new ArrayList<String>();
					String relation = "";
					try {
						if(page != null && page.getText().length() > 0 && page.getInfoBox() != null){
							sb.append(parseData(page));
						}	
						
					} catch (DOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println(relation);
					}
					catch(NullPointerException e){
						e.printStackTrace();
						System.out.println(relation);
					}
					catch(StringIndexOutOfBoundsException e){
						
					}
				}

			});
			wxsp.parse();
		}
		catch(SAXParseException e){
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		try{
			writeToFile(sb);
			File file = new File("WikiInfobox.xml");
			FileWriter fileWriter = new FileWriter(file , true);
			BufferedWriter buffWriter = new BufferedWriter(fileWriter);
			buffWriter.flush();
			buffWriter.write("\n</mediawiki>");
			buffWriter.close();
		}
		catch(IOException e){
			
		}
	}
	public static void writeToFile(StringBuilder sb){
		try{
		File file = new File("WikiInfobox.xml");
		FileWriter fileWriter = new FileWriter(file , true);
		BufferedWriter buffWriter = new BufferedWriter(fileWriter);
		buffWriter.flush();
			buffWriter.write(sb.toString());
		buffWriter.close();
		}
		catch(IOException e){
			
		}
	}
	public static String parseData(WikiPage page){
		StringBuffer pageText = new StringBuffer();
		if(page != null && page.getInfoBox() != null){
			pageText.append("<page>\n");
			pageText.append("<title>"+processPageText(page.getTitle())+"</title>\n");
			pageText.append("<id>"+processPageText(page.getTitle())+"</id>\n");
			pageText.append(pageText+"<revision>\n");
			pageText.append("<text xml:space=\"preserve\" bytes=\"42403\">\n"+processPageText(page.getInfoBox().dumpRaw())+"\n"+processPageText(page.getCategories().toString())+"\n</text>\n");
			pageText.append("<model>wikitext</model>\n");
			pageText.append("<format>text/x-wiki</format>\n");
			pageText.append("</revision>\n");
			pageText.append("</page>\n");	
		}
		return pageText.toString();
	}
	public static String processPageText(String dump){
		dump = dump.replaceAll("&([a-zA-Z0-9]+?);", "");
		dump = dump.replace("&", " and ");
		dump = dump.replaceAll("<ref([a-zA-Z]+?)?", "");
		dump = dump.replaceAll("<", "");
		dump = dump.replaceAll(">", "");
		return dump;
		
	}
}
