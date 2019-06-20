package eg.edu.alexu.csd.filestructure.btree.implementation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eg.edu.alexu.csd.filestructure.btree.ISearchEngine;
import eg.edu.alexu.csd.filestructure.btree.ISearchResult;

public class SearchEngine implements ISearchEngine {

	@Override
	public void indexWebPage(String filePath) {
		// TODO Auto-generated method stub

	}

	@Override
	public void indexDirectory(String directoryPath) {
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
		// TODO Auto-generated method stub

	}

	@Override
	public List<ISearchResult> searchByWordWithRanking(String word) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
		// TODO Auto-generated method stub
		return null;
	}

}
