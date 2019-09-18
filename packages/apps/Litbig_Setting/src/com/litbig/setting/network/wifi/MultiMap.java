package com.litbig.setting.network.wifi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MultiMap<K, V> {
	private final HashMap<K, List<V>> store = new HashMap<K, List<V>>();
	
	List<V> getAll(K key) {
		List<V> values = store.get(key);
		return values != null ? values : Collections.<V>emptyList();
	}
	
	void put(K key, V val) {
		List<V> curVals = store.get(key);
		if(curVals == null) {
			curVals = new ArrayList<V>(3);
			store.put(key, curVals);
		}
		curVals.add(val);
	}
}