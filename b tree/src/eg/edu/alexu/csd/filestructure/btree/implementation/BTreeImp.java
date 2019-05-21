package eg.edu.alexu.csd.filestructure.btree.implementation;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import eg.edu.alexu.csd.filestructure.btree.IBTree;
import eg.edu.alexu.csd.filestructure.btree.IBTreeNode;

public class BTreeImp<K extends Comparable<K>, V> implements IBTree<K, V> {
	private class BTreeNode <Key extends Comparable<Key>,Value> implements IBTreeNode<Key,Value>{
		private int numOfKeys;
		private boolean isLeaf;
		private List<Key> keys;
		private List<Value> values;
		private List<IBTreeNode<Key, Value>> children;
		public BTreeNode(){
			this.numOfKeys = 0;
			this.isLeaf = false; 
			this.keys = new ArrayList<>();
			this.values = new ArrayList<>();
			this.children = new ArrayList<>();
		}
		
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
	
	private int t;
	private IBTreeNode<K, V> root;
	
	public BTreeImp(int minimumDegree) {
		if(minimumDegree < 2) {
			throw new RuntimeErrorException(null);
		}
		this.t = minimumDegree;
	}
	@Override
	public int getMinimumDegree() {
		return t;
	}

	@Override
	public IBTreeNode<K, V> getRoot() {
		return root;
	}

	@Override
	public void insert(K key, V value) {
		if (key == null || value == null) {
			throw new RuntimeErrorException(null);
		}
		if (root == null) {
			root = new BTreeNode<>();
			root.getKeys().add(key);
			root.getValues().add(value);
			root.setNumOfKeys(1);
			root.setLeaf(true);
		}
		
		if(root.getNumOfKeys() == (2*t-1)) {//full
			IBTreeNode<K, V> oldRoot = root;
			root = new BTreeNode<>();
			root.getChildren().add(oldRoot);
			split(root, 0, oldRoot);
		}
		IBTreeNode<K, V> nonFullLeaf = insertNonFull(key, root);
		
		if(nonFullLeaf != null) {
			int index = 0;
			while (index < nonFullLeaf.getNumOfKeys() && key.compareTo(nonFullLeaf.getKeys().get(index)) > 0) {
				index++;
			}			
			if(index < nonFullLeaf.getNumOfKeys() && key.compareTo(nonFullLeaf.getKeys().get(index)) == 0) {
				return;
			}else {
				nonFullLeaf.getKeys().add(index, key);
				nonFullLeaf.getValues().add(index, value);
				nonFullLeaf.setNumOfKeys(nonFullLeaf.getNumOfKeys() + 1);
			}
		}
		
	}
	
	//index is where the promoted key will be (index of node)
	private void split(IBTreeNode<K, V> parent, int index, IBTreeNode<K, V> node){
		IBTreeNode<K, V> secondNode = new BTreeNode<>();
		
		//handle parent
		parent.getKeys().add(index, node.getKeys().get(t-1));
		parent.getValues().add(index, node.getValues().get(t-1));
		parent.getChildren().add(index + 1, secondNode);
		parent.setNumOfKeys(parent.getNumOfKeys() + 1);
		
		//handle node & secondNode (keys, values,numOfKeys, children, leaf)
		secondNode.setKeys(new ArrayList<>(node.getKeys().subList(t, 2*t - 1)));
		node.getKeys().subList(t - 1, 2*t - 1).clear();
		
		secondNode.setValues(new ArrayList<>(node.getValues().subList(t, 2*t - 1)));
		node.getValues().subList(t - 1, 2*t - 1).clear();
		
		secondNode.setNumOfKeys(t - 1);
		node.setNumOfKeys(t - 1);
		secondNode.setLeaf(node.isLeaf());
		
		//handle children if not leaf
		if(!node.isLeaf()) {
			secondNode.setChildren(new ArrayList<>(node.getChildren().subList(t, 2*t)));
			node.getChildren().subList(t, 2*t).clear();
		}
	}
	
	private IBTreeNode<K, V> insertNonFull(K key,final IBTreeNode<K, V> x) {
		if (x.isLeaf()) {
			return x;
		}
		
		int index = 0;
		while (index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) > 0) {
			index++;
		}
		
		if(index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) == 0) {
			return null;
		}else {
			if(x.getChildren().get(index).getNumOfKeys() == (2*t-1)) {
				split(x, index, x.getChildren().get(index));
			}
			if(index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) > 0) {
				index++;
			}else if(index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) == 0) {
				return null;
			}
			return insertNonFull(key, x.getChildren().get(index));
		}
	}
	
	@Override
	public V search(K key) {
		if (key == null) {
			throw new RuntimeErrorException(null);
		}
		return BTreeSearch(key, root);
	}
	
	private V BTreeSearch(K key,IBTreeNode<K, V> x) {
		int i = 0;
		while (i < x.getNumOfKeys()&& key.compareTo(x.getKeys().get(i)) > 0) {
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
