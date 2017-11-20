package com.womai.wms.rf.common.util;

import com.womai.wms.rf.common.constants.Constants;

import java.security.MessageDigest;
/**
 * 字符串加密类
 *
 */
public class MD5Util {
	/** 加密生成一个32长度的16进制数字
	 * 加密字符串  
	 * @param value :原始字符串(非空)
	 * @return 返回一个32位的加密数据
	 */
    public static String encodeString(String value){
        return encodeString(encodeString(value,0),1);
    }

    /**
     *
     * @param value 原始字符串（非空）
     * @param type 0 字母小写。1 字母大写
     * @return 返回一个32位的加密数据
     */
	public static String encodeString(String value,int type){
		StringBuilder sb=new StringBuilder();
		try {
			MessageDigest messageDigest=MessageDigest.getInstance(Constants.MD5);
			byte[] bytes=messageDigest.digest(value.getBytes());//字节数组长度16
			for(int i=0;i<bytes.length;i++){//转换成十六进制，小于16的，不够两位，补0
				int tempInt=bytes[i]&0xff;
				if(tempInt<16){
					sb.append(0);
				}
                if(type==0){
                    sb.append(Integer.toHexString(tempInt));
                }else{
                    sb.append(Integer.toHexString(tempInt).toString().toUpperCase());
                }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
