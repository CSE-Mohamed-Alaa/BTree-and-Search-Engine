package eg.edu.alexu.csd.filestructure.btree.implementation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
		if(filePath == null) {
			throw new RuntimeErrorException(null);
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
		if(directoryPath == null) {
			throw new RuntimeErrorException(null);
		}
		File directory = new File(directoryPath);
		if(!directory.exists()) {
			throw new RuntimeErrorException(null);
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
		if(filePath == null) {
			throw new RuntimeErrorException(null);
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
		if(word == null) {
			throw new RuntimeErrorException(null);
		}
		List<ISearchResult> ans = bTree.search(word.trim().toLowerCase());
		return ans != null ? ans : new ArrayList<ISearchResult>();
	}

	@Override
	public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
		if (sentence == null) {
			throw new RuntimeErrorException(null);
		}
		List<ISearchResult> ans = new ArrayList<>();
		sentence = sentence.trim();
		String [] words = sentence.split("\\s+");
		List<HashMap<String,ISearchResult>> wordsmap = new ArrayList<>();
		for(String word : words) {
			List<ISearchResult> wordList = bTree.search(word.toLowerCase());
			if(wordList == null)return ans;
			HashMap<String,ISearchResult> map = new HashMap<>();
			for (int i = 0; i < wordList.size(); i++) {
				map.put(wordList.get(i).getId(), wordList.get(i));
			}
			wordsmap.add(map);
		}
		int min = 0;
		for (int i = 0; i < wordsmap.size(); i++) {
			if (wordsmap.get(i).size() < wordsmap.get(min).size()) {
				min = i;
			}
		}
		 
		ArrayList<ISearchResult>smallMapValues = new ArrayList<>();
		smallMapValues.addAll(wordsmap.get(min).values());
		for (int i = 0; i < smallMapValues.size(); i++) {
			boolean common = true;
			int smallestFreq= Integer.MAX_VALUE;
			for (int j = 0; j < wordsmap.size(); j++) {
				if(!wordsmap.get(j).containsKey(smallMapValues.get(i).getId())) {
					common = false;
					break;
				}
				smallestFreq = Math.min(smallestFreq, wordsmap.get(j).get(smallMapValues.get(i).getId()).getRank());
			}
			if(common) {
				ans.add(new SearchResult(smallMapValues.get(i).getId(), smallestFreq));
			}
		}
		
		return ans;
	}

	/**
	 * Read XML file with DOM parser
	 * 
	 * @param filePath
	 * @return List of pair contains <id, text> for every document in the file
	 */
	private List<Pair<String, String>> parseXML(String filePath) {
		File f = new File(filePath);
		if(!f.exists()) {
			throw new RuntimeErrorException(null);
		}
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
		} catch (SAXException | IOException | ParserConfigurationException | IllegalArgumentException e) {
			throw new RuntimeErrorException(null);
		}
		return docArray;
	}

}
