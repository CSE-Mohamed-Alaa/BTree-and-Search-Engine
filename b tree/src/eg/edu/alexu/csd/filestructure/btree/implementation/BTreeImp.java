package eg.edu.alexu.csd.filestructure.btree.implementation;

import java.util.List;

import eg.edu.alexu.csd.filestructure.btree.IBTree;
import eg.edu.alexu.csd.filestructure.btree.IBTreeNode;

public class BTreeImp<K extends Comparable<K>, V> implements IBTree<K, V> {
	private class BtreeNode <Key extends Comparable<Key>,Value> implements IBTreeNode<Key,Value>{
		private int numOfKeys;
		private boolean isLeaf;
		private List<Key> keys;
		private List<Value> values;
		private List<IBTreeNode<Key, Value>> children;
		@Override
		public int getNumOfKeys() {
			return numOfKeys;
		}

		@Override
		public void setNumOfKeys(int numOfKeys) {
			this.numOfKeys = numOfKeys;
		}

		@Override
		public boolean isLeaf() {
			return isLeaf;
		}

		@Override
		public void setLeaf(boolean isLeaf) {
			this.isLeaf = isLeaf;
		}

		@Override
		public List<Key> getKeys() {
			return keys;
		}

		@Override
		public void setKeys(List<Key> keys) {
			this.keys = keys;			
		}

		@Override
		public List<Value> getValues() {
			return values;
		}

		@Override
		public void setValues(List<Value> values) {
			this.values = values; 
		}

		@Override
		public List<IBTreeNode<Key, Value>> getChildren() {
			return children;
		}

		@Override
		public void setChildren(List<IBTreeNode<Key, Value>> children) {
			this.children = children;
			
		}
		
	}
	
	private int minimumDegree;
	private IBTreeNode<K, V> root;
	public BTreeImp(int minimumDegree) {
		this.minimumDegree = minimumDegree;
	}
	@Override
	public int getMinimumDegree() {
		return minimumDegree;
	}

	@Override
	public IBTreeNode<K, V> getRoot() {
		return root;
	}

	@Override
	public void insert(K key, V value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public V search(K key) {
		return BTreeSearch(key, root);
	}
	
	private V BTreeSearch(K key,IBTreeNode<K, V> x) {
		int i = 0;
		while (i < x.getNumOfKeys()&& key.compareTo(x.getKeys().get(i)) < 0) {
			i++;
		}
		if(i<x.getNumOfKeys()&& key.compareTo(x.getKeys().get(i)) == 0) {
			return x.getValues().get(i);
		}else if(x.isLeaf()) {
			return null;
		}else
			return BTreeSearch(key, x.getChildren().get(i));
	}

	@Override
	public boolean delete(K key) {
		// TODO Auto-generated method stub
		return false;
	}

}
