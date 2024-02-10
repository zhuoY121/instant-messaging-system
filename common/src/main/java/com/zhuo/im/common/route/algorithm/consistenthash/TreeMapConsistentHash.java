package com.zhuo.im.common.route.algorithm.consistenthash;

import com.zhuo.im.common.enums.UserErrorCode;
import com.zhuo.im.common.exception.ApplicationException;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @description:
 * @version: 1.0
 */
public class TreeMapConsistentHash extends AbstractConsistentHash {

    private TreeMap<Long, String> treeMap = new TreeMap<>();

    private static final int VIRTUAL_NODE_SIZE = 2;

    @Override
    protected void add(long key, String value) {
        for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
            treeMap.put(super.hash("node" + key + i), value);
        }
        treeMap.put(key, value);
    }

    
    @Override
    protected String getFirstNode(String value) {

        Long hash = super.hash(value);
        SortedMap<Long, String> last = treeMap.tailMap(hash);
        if (!last.isEmpty()) {
            return last.get(last.firstKey());
        }

        if (treeMap.size() == 0) {
            throw new ApplicationException(UserErrorCode.SERVICE_NOT_AVAILABLE) ;
        }

        return treeMap.firstEntry().getValue();
    }

    @Override
    protected void processBefore() {
        treeMap.clear();
    }
}
