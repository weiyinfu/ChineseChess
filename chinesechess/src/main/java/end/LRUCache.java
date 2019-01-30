package end;

import java.util.LinkedHashMap;

//在内存中始终存储至多7种局面，因为有7种棋子，当前局面至多依赖7种棋型
public class LRUCache<K, V> {
LinkedHashMap<K, V> ma = new LinkedHashMap<>();
int sz;

LRUCache(int size) {
    sz = size;
}

V get(K k) {
    V v = ma.get(k);
    ma.remove(k);
    ma.put(k, v);
    return v;
}

void put(K k, V v) {
    if (!ma.containsKey(k)) {
        ma.put(k, v);
    } else {
        ma.remove(k);
        ma.put(k, v);
    }
}
}
