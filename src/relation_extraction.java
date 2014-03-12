

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

public class relation_extraction {
	static final Pattern infocontentpat = Pattern.compile(
			"\\{\\{ *[A-Za-z]+obox.*?(}}[\n ]*[A-Za-z0-9'\"\\[])",
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
	static Element revisionElement = null;
	static Element idElement = null;
	static int count = 0;
	static HashMap<String, Integer> relationhashmap = new HashMap<String, Integer>();
	static HashSet<String> infohashset = BaseTrees.getInfoBoxSet();
	static int pageCount = 0;

	public static void main(String[] args) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();

		WikiXMLParser wxsp = WikiXMLParserFactory
				.getSAXParser("WikiDump_1600.xml");
				//.getSAXParser("/media/Arafath 1TB/Information Retrieval/wiki en xml/enwiki-20131001-pages-articles-multistream.xml");
		long starttime = System.currentTimeMillis();
		try {
			docBuilder = docFactory.newDocumentBuilder();

			// root elements
			doc = docBuilder.newDocument();
			rootElement = doc.createElement("mediawiki");
			doc.appendChild(rootElement);

			wxsp.setPageCallback(new PageCallbackHandler() {
				public void process(WikiPage page) {

					String relation = "";
					try {
						String infotype = "";
						Matcher infotypePatttern = infotypepat.matcher(page
								.getWikiText());
						if (infotypePatttern.find()) {
							infotype = infotypePatttern.group(1).toString()
									.trim();

						}

						if (infotype != "" && (infotype.trim().toLowerCase().compareTo("film") == 0 
								|| infotype.trim().toLowerCase().compareTo("organization") == 0
								|| infotype.trim().toLowerCase().compareTo("musical artist") == 0)) {
							// System.out.println("Title: " + page.getTitle());
							// System.out.println("InfoBox Type: " + infotype);

							//if (infohashset.contains(infotype.toLowerCase())) {
								peoplePage = true;
								pageCount++;
								// System.out.println(infotype);
								//System.out.println(page.getTitle());
							//}

							if (peoplePage) {
								pageElement = doc.createElement("page");
								rootElement.appendChild(pageElement);
								revisionElement = doc.createElement("revision");
								pageElement.appendChild(revisionElement);
								if(page.getID()=="2819190"){
									relation = relation.trim();
								}
								Matcher pat3 = infocontentpat.matcher(page
										.getWikiText());
								if (pat3.find()) {
									String tmpText = pat3.group(0);
									Matcher categoriesPat= catpat.matcher(page.getWikiText());
									if(categoriesPat.find()){
										tmpText = tmpText+"\n"+categoriesPat.group(0);
									}
									// System.out.println(tmpText);
									// add title
									Element title = doc.createElement("title");
									title.appendChild(doc.createTextNode(page
											.getTitle()));
									revisionElement.appendChild(title);
									// add title
									Element id = doc.createElement("id");
									id.appendChild(doc.createTextNode(page
											.getID()));
									revisionElement.appendChild(id);
									Element text = doc.createElement("text");
									text.appendChild(doc.createTextNode(tmpText));
									revisionElement.appendChild(text);
								}
								peoplePage = false;
							}
						}
					} catch (DOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println(relation);
					}
				}

			});

			wxsp.parse();
			try {

				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File("TestInfoboxDump.xml"));
				transformer.transform(source, result);

			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		long endtime = System.currentTimeMillis();
		int minutes = (int) (((endtime - starttime) / 1000) / 60);
		int seconds = (int) (((endtime - starttime) / 1000) % 60);
		System.out.println(minutes + ":" + seconds);
		System.out.println(relationhashmap.size());
		System.out.println(pageCount);
		TreeMap<String, Integer> sortedpostingsmap = (TreeMap<String, Integer>) sortByValues(relationhashmap);
		writeTreetoFile(sortedpostingsmap);
	}

	protected static void appendElements(Element pageEl, String relation,
			String relationtext) {		
		Element something = doc.createElement(relation);
		something.appendChild(doc.createTextNode(relationtext));
		pageElement.appendChild(something);

	}

	private static void writeTreetoFile(
			TreeMap<String, Integer> sortedpostingsmap) {

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

}
