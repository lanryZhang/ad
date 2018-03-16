/*
* MD5Util.java 
* Created on  202018/1/11 14:12 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class MD5Util {
    /**
     * 32位MD5加密
     * @param plainText
     * @return
     */
    public static String encryption32(String plainText) {
        try {
            String re_md5 = new String();
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(plainText.getBytes());
                byte b[] = md.digest();

                int i;

                StringBuffer buf = new StringBuffer("");
                for (int offset = 0; offset < b.length; offset++) {
                    i = b[offset];
                    if (i < 0)
                        i += 256;
                    if (i < 16)
                        buf.append("0");
                    buf.append(Integer.toHexString(i));
                }

                re_md5 = buf.toString();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return re_md5;
        }catch (Exception er){
            er.printStackTrace();
        }
        return "";
    }


}
