package com.zhuo.im.common.route.algorithm.consistenthash;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @description: Consistent hashing. abstract class
 * @version: 1.0
 */
public abstract class AbstractConsistentHash {

    protected abstract void add(long key, String value);

    protected void sort() {}

    protected abstract String getFirstNode(String value);


    protected abstract void processBefore();

    /**
     * @description Pass in the node list and client information to obtain a service node
     * @return
     */
    public synchronized String process(List<String> values, String key){
        processBefore();
        for (String value : values) {
            add(hash(value), value);
        }
        sort();
        return getFirstNode(key) ;
    }


    /**
     * @description Calculate the hash value
     * @return
     */
    public Long hash(String value){

        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset();
        byte[] keyBytes = null;
        try {
            keyBytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unknown string :" + value, e);
        }

        md5.update(keyBytes);
        byte[] digest = md5.digest();

        // hash code, truncate to 32-bits
        long hashCode = ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);

        long truncateHashCode = hashCode & 0xffffffffL;
        return truncateHashCode;
    }

}
