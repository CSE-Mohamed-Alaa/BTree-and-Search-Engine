package eg.edu.alexu.csd.filestructure.btree.implementation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eg.edu.alexu.csd.filestructure.btree.IBTree;
import eg.edu.alexu.csd.filestructure.btree.ISearchEngine;
import eg.edu.alexu.csd.filestructure.btree.ISearchResult;
import javafx.util.Pair;

public class SearchEngine implements ISearchEngine {
	IBTree<String, List<ISearchResult>> bTree;

	public SearchEngine(int t) {
		bTree = new BTreeImp<String, List<ISearchResult>>(t);
	}

	@Override
	public void indexWebPage(String filePath) {
		List<Pair<String, String>> docArray = parseXML(filePath);
		Pattern p = Pattern.compile("\\w+");
		for (Pair<String, String> doc : docArray) {
			String id = doc.getKey();
			Matcher m = p.matcher(doc.getValue());
			List<String> words = new ArrayList<>();
			while (m.find()) {
				words.add(m.group());
			}
			Map<String, Integer> results = new HashMap<>();
			for (String word : words) {
				String lowerCaseWord = word.toLowerCase();
				results.put(lowerCaseWord, results.getOrDefault(lowerCaseWord, 0) + 1);
			}
			for (Map.Entry<String, Integer> wordRankPair : results.entrySet()) {
				List<ISearchResult> searchResultList;
				searchResultList = bTree.search(wordRankPair.getKey());
				if (searchResultList != null) {
					boolean docIndexedBefore = false;
					for (ISearchResult searchResult : searchResultList) {
						if (id.equals(searchResult.getId())) {
							docIndexedBefore = true;
							searchResult.setRank(wordRankPair.getValue());
							break;
						}
					}
					if (!docIndexedBefore) {
						searchResultList.add(new SearchResult(id, wordRankPair.getValue()));
					}

				} else {
					searchResultList = new ArrayList<>();
					searchResultList.add(new SearchResult(id, wordRankPair.getValue()));
					bTree.insert(wordRankPair.getKey(), searchResultList);
				}
			}
		}

	}

	@Override
	public void indexDirectory(String directoryPath) {
		File directory = new File(directoryPath);
		if(!directory.exists()) {
			return;
		}
		List<File> files = new ArrayList<>();
		listf(directoryPath, files);
		for (File file : files) {
			indexWebPage(file.getAbsolutePath());
		}
	}

	private void listf(String directoryName, List<File> files) {
		File directory = new File(directoryName);
		File[] fList = directory.listFiles();
		if (fList != null) {
			for (File file : fList) {
				if (file.isFile()) {
					files.add(file);
				} else if (file.isDirectory()) {
					listf(file.getAbsolutePath(), files);
				}
			}
		}
	}

	@Override
	public void deleteWebPage(String filePath) {
		File file = new File(filePath);
		if(!file.exists()) {
			return;
		}
		List<Pair<String, String>> docArray = parseXML(filePath);
		Pattern p = Pattern.compile("\\w+");
		for (Pair<String, String> doc : docArray) {
			String id = doc.getKey();
			Matcher m = p.matcher(doc.getValue());
			List<String> words = new ArrayList<>();
			while (m.find()) {
				words.add(m.group());
			}
			Map<String, Integer> results = new HashMap<>();
			for (String word : words) {
				String lowerCaseWord = word.toLowerCase();
				results.put(lowerCaseWord, results.getOrDefault(lowerCaseWord, 0) + 1);
			}
			for (Map.Entry<String, Integer> wordRankPair : results.entrySet()) {
				List<ISearchResult> searchResultList;
				searchResultList = bTree.search(wordRankPair.getKey());
				if (searchResultList != null) {
					for (ISearchResult searchResult : searchResultList) {
						if (id.equals(searchResult.getId())) {
							searchResultList.remove(searchResult);
							if (searchResultList.isEmpty()) {
								bTree.delete(wordRankPair.getKey());
							}
							break;
						}
					}
				}
			}
		}

	}

	@Override
	public List<ISearchResult> searchByWordWithRanking(String word) {
		return bTree.search(word.toLowerCase());
	}

	@Override
	public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Read XML file with DOM parser
	 * 
	 * @param filePath
	 * @return List of pair contains <id, text> for every document in the file
	 */
	private List<Pair<String, String>> parseXML(String filePath) {
		List<Pair<String, String>> docArray = new ArrayList<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			Document file = docBuilder.parse(filePath);
			NodeList docList = file.getElementsByTagName("doc");
			for (int i = 0; i < docList.getLength(); i++) {
				Node docNode = docList.item(i);
				if (docNode.getNodeType() == Node.ELEMENT_NODE) {
					Element docElement = (Element) docNode;
					docArray.add(new Pair<String, String>(docElement.getAttribute("id"), docElement.getTextContent()));
				}
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeErrorException(null);
		}
		return docArray;
	}

	public static void main(String[] arg) {
		SearchEngine x = new SearchEngine(100);
		x.indexWebPage("res\\wiki_00");
		System.out.println();
	}

}
