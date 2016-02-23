/*
 * Copyright (c): cbb 2016

 */
package org.apache.camel.example.spring.javaconfig;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.camel.Exchange;

/**
 * @author cbb
 */
public class HashUtil {
    
    public static MessageDigest hash(Exchange exchange) throws NoSuchAlgorithmException{
        File f = exchange.getIn().getBody(File.class);
        String myHash = "MD5";
        MessageDigest complete = MessageDigest.getInstance(myHash);
        return complete;
    }
}
