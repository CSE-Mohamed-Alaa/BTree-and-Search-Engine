package eg.edu.alexu.csd.filestructure.btree.implementation;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

import javax.management.RuntimeErrorException;

import eg.edu.alexu.csd.filestructure.btree.IBTree;
import eg.edu.alexu.csd.filestructure.btree.IBTreeNode;

//TODO implement hard disk && test delete

public class BTreeImp<K extends Comparable<K>, V> implements IBTree<K, V> {
	private class BTreeNode<Key extends Comparable<Key>, Value> implements IBTreeNode<Key, Value> {
		private int numOfKeys;
		private boolean isLeaf;
		private List<Key> keys;
		private List<Value> values;
		private List<IBTreeNode<Key, Value>> children;

		public BTreeNode() {
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
		if (minimumDegree < 2) {
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

		if (root.getNumOfKeys() == (2 * t - 1)) {// full
			IBTreeNode<K, V> oldRoot = root;
			root = new BTreeNode<>();
			root.getChildren().add(oldRoot);
			split(root, 0);
		}
		IBTreeNode<K, V> nonFullLeaf = insertNonFull(key, root);

		if (nonFullLeaf != null) {
			int index = 0;
			while (index < nonFullLeaf.getNumOfKeys() && key.compareTo(nonFullLeaf.getKeys().get(index)) > 0) {
				index++;
			}
			if (index < nonFullLeaf.getNumOfKeys() && key.compareTo(nonFullLeaf.getKeys().get(index)) == 0) {
				return;
			} else {
				nonFullLeaf.getKeys().add(index, key);
				nonFullLeaf.getValues().add(index, value);
				nonFullLeaf.setNumOfKeys(nonFullLeaf.getNumOfKeys() + 1);
			}
		}

	}

	// index is where the promoted key will be (index of node)
	private void split(IBTreeNode<K, V> x, int index) {
		IBTreeNode<K, V> y = x.getChildren().get(index);
		IBTreeNode<K, V> z = new BTreeNode<>();

		// handle parent
		x.getKeys().add(index, y.getKeys().get(t - 1));
		x.getValues().add(index, y.getValues().get(t - 1));
		x.getChildren().add(index + 1, z);
		x.setNumOfKeys(x.getNumOfKeys() + 1);

		// handle node & secondNode (keys, values,numOfKeys, children, leaf)
		z.setKeys(new ArrayList<>(y.getKeys().subList(t, 2 * t - 1)));
		y.getKeys().subList(t - 1, 2 * t - 1).clear();

		z.setValues(new ArrayList<>(y.getValues().subList(t, 2 * t - 1)));
		y.getValues().subList(t - 1, 2 * t - 1).clear();

		z.setNumOfKeys(t - 1);
		y.setNumOfKeys(t - 1);
		z.setLeaf(y.isLeaf());

		// handle children if not leaf
		if (!y.isLeaf()) {
			z.setChildren(new ArrayList<>(y.getChildren().subList(t, 2 * t)));
			y.getChildren().subList(t, 2 * t).clear();
		}
	}

	private IBTreeNode<K, V> insertNonFull(K key, final IBTreeNode<K, V> x) {
		if (x.isLeaf()) {
			return x;
		}

		int index = 0;
		while (index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) > 0) {
			index++;
		}

		if (index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) == 0) {
			return null;
		} else {
			if (x.getChildren().get(index).getNumOfKeys() == (2 * t - 1)) {
				split(x, index);
			}
			if (index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) > 0) {
				index++;
			} else if (index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) == 0) {
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

	private V BTreeSearch(K key, IBTreeNode<K, V> x) {
		int i = 0;
		while (i < x.getNumOfKeys() && key.compareTo(x.getKeys().get(i)) > 0) {
			i++;
		}
		if (i < x.getNumOfKeys() && key.compareTo(x.getKeys().get(i)) == 0) {
			return x.getValues().get(i);
		} else if (x.isLeaf()) {
			return null;
		} else
			return BTreeSearch(key, x.getChildren().get(i));
	}

	@Override
	public boolean delete(K key) {
		if (key == null || root == null) {
			throw new RuntimeErrorException(null);
		}
		if (root.isLeaf()) {
			int index = 0;
			while (index < root.getNumOfKeys() && key.compareTo(root.getKeys().get(index)) > 0) {
				index++;
			}
			if (index < root.getNumOfKeys() && key.compareTo(root.getKeys().get(index)) == 0) {
				root.getKeys().remove(index);
				root.getValues().remove(index);
				root.setNumOfKeys(root.getNumOfKeys() - 1);
				return true;
			} else {
				return false;
			}
		} else if (root.getNumOfKeys() == 1 && root.getChildren().get(0).getNumOfKeys() == (t - 1)
				&& root.getChildren().get(1).getNumOfKeys() == (t - 1)) {
			combine(root, 0);
			root = root.getChildren().get(0);
			return delete(key);
		} else {
			return bTreeDelete(key, root);
		}
	}

	private boolean bTreeDelete(K key, IBTreeNode<K, V> x) {
		if (x.isLeaf() && x.getNumOfKeys() > (t - 1)) {
			// case 1
			int index = 0;
			while (index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) > 0) {
				index++;
			}
			if (index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) == 0) {
				x.getKeys().remove(index);
				x.getValues().remove(index);
				x.setNumOfKeys(x.getNumOfKeys() - 1);
				return true;
			} else {
				return false;
			}
		} else {
			// case 2 or 3
			int index = 0;
			while (index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) > 0) {
				index++;
			}
			if (index < x.getNumOfKeys() && key.compareTo(x.getKeys().get(index)) == 0) {
				// case 2
				if (x.getChildren().get(index).getNumOfKeys() > (t - 1)) {
					// case 2A predecessor
					Pair<K, V> temp = bTreePredecessor(key, x.getChildren().get(index));
					x.getKeys().set(index, temp.getKey());
					x.getValues().set(index, temp.getValue());
					return bTreeDelete(key, x.getChildren().get(index));
				} else if (x.getChildren().get(index + 1).getNumOfKeys() > (t - 1)) {
					// case 2B successor
					Pair<K, V> temp = bTreeSuccessor(key, x.getChildren().get(index + 1));
					x.getKeys().set(index, temp.getKey());
					x.getValues().set(index, temp.getValue());
					return bTreeDelete(key, x.getChildren().get(index + 1));
				} else {
					// case 2C
					combine(x, index);
					return bTreeDelete(key, x.getChildren().get(index));
				}
			} else {
				if (x.isLeaf()) {
					return false;
				} else if (x.getChildren().get(index).getNumOfKeys() == (t - 1)) {
					// case 3
					if ((index + 1) <= x.getNumOfKeys() && x.getChildren().get(index + 1).getNumOfKeys() > (t - 1)) {
						// case 3A1
						IBTreeNode<K, V> y = x.getChildren().get(index);
						IBTreeNode<K, V> z = x.getChildren().get(index + 1);

						y.getKeys().add(x.getKeys().get(index));
						y.getValues().add(x.getValues().get(index));
						if (!y.isLeaf()) {
							y.getChildren().add(z.getChildren().remove(0));
						}
						y.setNumOfKeys(y.getNumOfKeys() + 1);

						x.getKeys().set(index, z.getKeys().remove(0));
						x.getValues().set(index, z.getValues().remove(0));
						z.setNumOfKeys(z.getNumOfKeys() - 1);

						return bTreeDelete(key, y);
					} else if ((index - 1) >= 0 && x.getChildren().get(index - 1).getNumOfKeys() > (t - 1)) {
						// case 3A2
						IBTreeNode<K, V> y = x.getChildren().get(index);
						IBTreeNode<K, V> z = x.getChildren().get(index - 1);

						y.getKeys().add(0, x.getKeys().get(index - 1));
						y.getValues().add(0, x.getValues().get(index - 1));
						if (!y.isLeaf()) {
							y.getChildren().add(0, z.getChildren().remove(z.getChildren().size() - 1));
						}
						y.setNumOfKeys(y.getNumOfKeys() + 1);

						x.getKeys().set(index - 1, z.getKeys().remove(z.getKeys().size() - 1));
						x.getValues().set(index - 1, z.getValues().remove(z.getValues().size() - 1));
						z.setNumOfKeys(z.getNumOfKeys() - 1);

						return bTreeDelete(key, y);
					} else {
						if ((index + 1) <= x.getNumOfKeys()
								&& x.getChildren().get(index + 1).getNumOfKeys() == (t - 1)) {
							// case 3B1
							combine(x, index);
							return bTreeDelete(key, x.getChildren().get(index));
						} else if ((index - 1) >= 0 && x.getChildren().get(index - 1).getNumOfKeys() == (t - 1)) {
							// case 3B2
							combine(x, index - 1);
							return bTreeDelete(key, x.getChildren().get(index - 1));
						}
					}
				} else {
					return bTreeDelete(key, x.getChildren().get(index));
				}
			}
		}

		return false;
	}

	private void combine(IBTreeNode<K, V> x, int index) {
		// y & z numOfKeys must be t-1
		IBTreeNode<K, V> y = x.getChildren().get(index);
		IBTreeNode<K, V> z = x.getChildren().remove(index + 1);

		// handle key in x (parent)
		y.getKeys().add(x.getKeys().remove(index));
		y.getValues().add(x.getValues().remove(index));
		x.setNumOfKeys(x.getNumOfKeys() - 1);

		// add z to y
		y.getKeys().addAll(z.getKeys());
		y.getValues().addAll(z.getValues());
		y.getChildren().addAll(z.getChildren());

		y.setNumOfKeys(2 * t - 1);
	}

	private Pair<K, V> bTreePredecessor(K key, IBTreeNode<K, V> subRoot) {
		int lastIndex = subRoot.getNumOfKeys() - 1;
		if (subRoot.isLeaf()) {
			Pair<K, V> temp = new Pair<>(subRoot.getKeys().get(lastIndex), subRoot.getValues().get(lastIndex));
			subRoot.getKeys().set(lastIndex, key);
			return temp;
		}
		return bTreePredecessor(key, subRoot.getChildren().get(lastIndex + 1));
	}

	private Pair<K, V> bTreeSuccessor(K key, IBTreeNode<K, V> subRoot) {
		if (subRoot.isLeaf()) {
			Pair<K, V> temp = new Pair<>(subRoot.getKeys().get(0), subRoot.getValues().get(0));
			subRoot.getKeys().set(0, key);
			return temp;
		}
		return bTreeSuccessor(key, subRoot.getChildren().get(0));
	}
}
