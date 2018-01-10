package de.nikxs.digitalstrom.vdc.server.util;

import java.util.concurrent.*;

/**
 * No additional use of such spin locks or synchronizers, to ensure concurrent thread safety,
 * in the case of concurrent high performance
 */
public class ConcurrentCache<K, V> implements Computable<K, V> {

    /**
     * Concurrent Security Map
     */
    private final ConcurrentMap<K, Future<V>> concurrentMap;

    /**
     * Creates a new instance of ConcurrentCache.
     */
    public ConcurrentCache() {
        concurrentMap = new ConcurrentHashMap<K, Future<V>>();
    }

    /**
     * The static method returns the computing interface
     *
     * @return
     */
    public static <K, V> Computable<K, V> createComputable() {
        return new ConcurrentCache<K, V>();
    }

    /**
     * Get data by keyword, return directly if it exists, or generate <code>callable</code> if it does not exist.
     *
     * @param key Find keywords
     * @param callable # @see Callable
     * @return Calculation results
     */
    public V get(K key, Callable<V> callable) {
        Future<V> future = concurrentMap.get(key);
        if (future == null) {
            FutureTask<V> futureTask = new FutureTask<V>(callable);
            future = concurrentMap.putIfAbsent(key, futureTask);
            if (future == null) {
                future = futureTask;
                futureTask.run();
            }
        }
        try {
            // At this point blocked
            return future.get();
        } catch (Exception e) {
            concurrentMap.remove(key);
            return null;
        }
    }
}
