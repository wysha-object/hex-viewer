package cn.com.wysha.hex_viewer.cache;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.Function;

public class LRUMapCache<K, V> {
    @Getter @Setter
    private int capacity;

    private final Function<K, V> loader;

    private final LinkedHashMap<K, V> map = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    };

    public LRUMapCache(int capacity, Function<K, V> loader) {
        this.capacity = capacity;
        this.loader = loader;
    }

    public synchronized V get(K key) {
        return map.computeIfAbsent(key, loader);
    }

    public synchronized void remove(K key) {
        map.remove(key);
    }

    public synchronized void clear() {
        map.clear();
    }
}
