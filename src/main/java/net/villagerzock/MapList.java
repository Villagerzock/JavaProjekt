package net.villagerzock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapList<K,V> extends HashMap<K, List<V>> {
    public void putItem(K key, V value){
        List<V> list = this.get(key);
        list.add(value);
    }
    @Override
    public List<V> get(Object key) {
        if (!containsKey(key)){
            List<V> list = new ArrayList<>();
            put((K) key,list);
        }
        return super.get(key);
    }
}
