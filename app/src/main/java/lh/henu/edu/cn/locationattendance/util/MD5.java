package lh.henu.edu.cn.locationattendance.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by bowen on 2017/10/15.
 */

public class MD5 {
    public static String toMd5String(String source) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        MessageDigest mDigest = MessageDigest.getInstance("md5");
        mDigest.update(source.getBytes("utf-8"));
        //获取加密密文
        byte[] encryptStr = mDigest.digest();
        char str[] = new char[encryptStr.length *2];
        int k = 0;
        //转化为16进制
        for(int i=0;i < encryptStr.length;i++)
        {
            byte byte0 = encryptStr[i];
            str[k++] = hexDigits[byte0>>>4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }

}
