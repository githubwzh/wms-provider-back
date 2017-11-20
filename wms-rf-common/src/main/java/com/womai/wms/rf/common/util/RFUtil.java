package com.womai.wms.rf.common.util;

import com.womai.wms.rf.common.constants.Constants;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xixiaochuan on 14-11-27.
 */
public class RFUtil {

    private final static int pdaWidth = 34;//pda一个屏幕能显示的字节数，英文一字节，汉字俩字节

    /**
     * 用map中的key为标识，以value替换字符串中的变量，例如 "第${currentPage}/${totalPage}页，共{totalCount}条数据"
     *
     * @param template 字符串模板
     * @param data     map传值
     * @return 返回替换后的字符串
     */
    public static String composeMessage(String template, Map<String, Object> data) {
        Iterator it = data.entrySet().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            template = template.replaceFirst("\\$\\{" + o.toString().split("=")[0] + "}", o.toString().split("=")[1]);
        }
        return template;
    }

    /**
     * 构造分页提示信息，显示当前页、总页数、总数据条数
     *
     * @param currentPage 当前页
     * @param totalPage   总页数
     * @param totalCount  数据总条数
     * @return 返回构造的分页提示信息
     */
    public static String getPageInfo(Integer currentPage, Integer totalPage, Integer totalCount) {
        Map<String, Object> pageMap = new HashMap<String, Object>();
        pageMap.put("currentPage", currentPage);
        pageMap.put("totalPage", totalPage);
        pageMap.put("totalCount", totalCount);
        return composeMessage(Constants.PAGE_INFO, pageMap);
    }

    /**
     * 按照标识符分割字符串，返回list
     *
     * @param resource 目前字符串
     * @param split    特定标识符
     * @return 构造的list
     */
    public static List str2List(String resource, String split) {
        List result = new ArrayList();
        String[] str = resource.split(split);
        for (int i = 0; i < str.length; i++) {
            if (StringUtils.isNotBlank(str[i])) {
                result.add(str[i].trim());
            }
        }
        return result;
    }


    /**
     * 转换为字节数组
     *
     * @param str
     * @return
     */
    public static byte[] getBytes(String str) {
        if (str != null) {
            try {
                return str.getBytes(Constants.CHARSET);
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 转换为字节数组
     *
     * @param bytes
     * @return
     */
    public static String toString(byte[] bytes) {
        try {
            return new String(bytes, Constants.CHARSET);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }


    /**
     * 获取当前字符串的字节长度
     *
     * @param s
     * @return
     */
    public static int getWordCount(String s) {

        s = s.replaceAll("[^\\x00-\\xff]", "**");//匹配双字节字符(包括汉字在内)：[^\x00-\xff]
        int length = s.length();
        return length;
    }



    /**
     * 将打印的内容调整为适合屏幕，超过屏幕的部分使用换行符分隔
     *
     * @param str       待处理的字符串
     * @param tip       待处理字符串的提示内容
     * @param lineLimit 限制多少行
     * @return 处理后的字符串
     */
    public static String makeStrFitPda(String str, String tip, int lineLimit) {
        String newStr = "";// 处理后的字符串
        int byteLength = 0; //字节长度
        int charLength = str.length(); // 字符长度
        for (int i = 0; i < charLength; i++) {
            int ascii = Character.codePointAt(str, i);
            if (ascii > 0 && ascii <= 255) {
                byteLength++;
            } else {
                byteLength += 2;
            }
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(tip)) {
            tip +=":";
            int tipCharLength = tip.length();
            int tipByteLength = 0;
            for (int i = 0; i < tipCharLength; i++) {
                int ascii = Character.codePointAt(tip, i);
                if (ascii > 0 && ascii <= 255) {
                    tipByteLength++;
                } else {
                    tipByteLength += 2;
                }
            }

            if (byteLength <= (pdaWidth - tipByteLength)) {
                return str; //如果字符串长度没超过屏幕，直接返回原字符串
            } else {
                //不考虑lineLimit的限制的话需要多少行
                int needLines = (byteLength + tipByteLength) % pdaWidth == 0 ? (byteLength + tipByteLength) / pdaWidth : ((byteLength + tipByteLength) / pdaWidth + 1);
                int start = 0;//本次开始字段
                int end = 0;//截取到的位置
                for (int i = 0; i < lineLimit; i++) {
                    if (i == 0) {//第一次截取有点儿特别，需要去掉tip的长度，并且不需要增加换行符
                        end = subBytes(str, start, pdaWidth - tipByteLength);
                        newStr += str.substring(start, end);
                    } else if (i == (needLines - 1) || i == (lineLimit - 1)) {
                        newStr += Constants.BREAK_LINE+str.substring(end);
                        break;
                    } else {
                        start = end;//本次开始字段等于上次结束字段
                        end = subBytes(str, start, pdaWidth);
                        newStr += Constants.BREAK_LINE+str.substring(start, end);
                    }
                }

            }

        } else {
            if (byteLength <= (pdaWidth )) {
                return str; //如果字符串长度没超过屏幕，直接返回原字符串
            } else {
                //不考虑lineLimit的限制的话需要多少行
                int needLines = byteLength % pdaWidth == 0 ? byteLength / pdaWidth : (byteLength / pdaWidth + 1);
                int start = 0;//本次开始字段
                int end = 0;//截取到的位置
                for (int i = 0; i < lineLimit; i++) {
                    if (i == 0) {//第一次截取有点儿特别,不需要增加换行符
                        end = subBytes(str, start, pdaWidth);
                        newStr += str.substring(start, end);
                    } else if (i == (needLines - 1) || i == (lineLimit - 1)) {
                        newStr += Constants.BREAK_LINE + str.substring(end);
                        break;
                    } else {
                        start = end;//本次开始字段等于上次结束字段
                        end = subBytes(str, start, pdaWidth);
                        newStr += Constants.BREAK_LINE + str.substring(start, end);
                    }
                }
            }
        }
        return newStr;

    }

    /**
     * 截取字符串的若干字节长度后应该截取到哪个位置
     *
     * @param str        待截取字符串
     * @param start      起始位置
     * @param byteLength 截取的字节长度
     * @return 应该截取到字符串的位置
     */
    public static int  subBytes(String str, int start, int byteLength) {
        int end = 0;
        int length = str.length();
        int byteCounts = 0;
        for(int i=start;i<length;i++){
            int ascii = Character.codePointAt(str, i);
            if (ascii > 0 && ascii <= 255) {
                byteCounts++;
            } else {
                byteCounts += 2;
            }
            // 若当前累计的字节数大于要截取的字节数-1，截取字符串组最长的情况是本次循环是汉字
            //并且上次循环后byteCounts=byteLength-2，则本次循环是一个汉字的话byteCounts=byteLength，那么截到当前位置刚好
            if(byteCounts>=(byteLength-1)){
                end = i+1;
                break;
            }
        }
        return end;
    }

    /**
     * 对长宽高做升序排序
     * @param decimals 存放长宽高的数组
     * @return 从小到大排序后的数组
     */
    public static BigDecimal[] getLengthOfSide(BigDecimal[] decimals) {
        BigDecimal temp;
        for(int j=1;j<decimals.length;j++) {
            for (int i = 0; i < decimals.length - j; i++) {
                if (decimals[i].compareTo(decimals[i + 1]) == 1) {
                    temp = decimals[i];
                    decimals[i] = decimals[i + 1];
                    decimals[i + 1] = temp;
                }
            }
        }
        return decimals;
    }
}
