package com.womai.wms.rf.common.util;

import org.apache.commons.lang.StringUtils;

/**
 * User: zhangwei
 * Date: 2016-05-03
 * To change this template use File | Settings | File Templates.
 */
public class RegExpUtil {

    /**
     * 匹配纯数字
     * @param str 待匹配字符串
     * @return 匹配返回true,不匹配返回false
     */
    public static boolean matchPureNum(String str){
        String reg = "^[0-9]*$";
        if(StringUtils.isBlank(str)){
            return false;
        }
        return str.matches(reg);
    }

}
