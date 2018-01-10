package de.nikxs.digitalstrom.vdc.server.util;

import java.util.concurrent.Callable;

/**
 * Interface for calculating type tasks
 */
public interface Computable <K, V> {

    /**
     * Calculated by keywords
     *
     * @param key Find keywords
     * @param callable
     *            # @see Callable
     * @return Calculation results

     */
    V get(K key, Callable<V> callable);


}
