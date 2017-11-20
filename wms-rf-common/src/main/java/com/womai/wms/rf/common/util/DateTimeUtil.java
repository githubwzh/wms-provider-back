package com.womai.wms.rf.common.util;

import com.womai.wms.rf.common.constants.TipConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 日期时间工具类
 * User:zhangwei
 * Date: 2016-07-01
 * To change this template use File | Settings | File Templates.
 */
public class DateTimeUtil {

    private final static String YYYYMMDD_SIMPLE = "yyyyMMdd";// 简单的年月日格式
    private final static String YYYYMMDD_SEPARATOR = "yyyy-MM-dd";// 带分隔符的日期格式
    private static final SimpleDateFormat SDF_SIMPLE = new SimpleDateFormat(YYYYMMDD_SIMPLE);// 简单日期格式化对象
    private static final SimpleDateFormat SDF_SEPARATOR = new SimpleDateFormat(YYYYMMDD_SEPARATOR);// 带分隔符的日期格式化对象

    private static ThreadLocal<SimpleDateFormat> localSimpleDateSeparator = new ThreadLocal<SimpleDateFormat>(){
        public SimpleDateFormat initialValue(){
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private static ThreadLocal<SimpleDateFormat> localSimpleDateSimple = new ThreadLocal<SimpleDateFormat>(){
        public SimpleDateFormat initialValue(){
            return new SimpleDateFormat("yyyyMMdd");
        }
    };

    /**
     * 获取带分隔符的日期字符串yyyy-MM-dd
     *
     * @param date 日期
     * @return 带分隔符的日期字符串
     */
    public static String getStringWithSeparator(Date date) {
//        SimpleDateFormat myDateFormat = new SimpleDateFormat(YYYYMMDD_SEPARATOR);
        SimpleDateFormat myDateFormat = localSimpleDateSeparator.get(); //new SimpleDateFormat(YYYYMMDD_SEPARATOR);
        return myDateFormat.format(date);
    }

    /**
     * 获取简单类型的日期字符串yyyyMMdd
     *
     * @param date 日期对象
     * @return 简单类型的日期字符串
     */
    public static String getStringSimple(Date date) {
//        SimpleDateFormat mySimpleData = new SimpleDateFormat(YYYYMMDD_SIMPLE);
        SimpleDateFormat mySimpleData = localSimpleDateSimple.get();
        return mySimpleData.format(date);
    }

    /**
     * 将简单类型yyyyMMdd日期字符串转化为日期
     *
     * @param str 日期字符串
     * @return 转换后的日期对象
     */
    public static Date parseSimpleStr(String str) {
        try {
            //为了避免多线程环境下发生错误，每次new一个SimpleDateFormat对象(略微影响性能)
//            SimpleDateFormat myDateFormat = new SimpleDateFormat(YYYYMMDD_SIMPLE);
            SimpleDateFormat myDateFormat = localSimpleDateSimple.get();
            return myDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将带分隔符的日期字符串yyyy-MM-dd转化为日期
     *
     * @param str 日期字符串
     * @return 转换后的日期对象
     */
    public static Date parseToDateWithShort(String str) {
        try {
//            SimpleDateFormat myDateFormat = new SimpleDateFormat(YYYYMMDD_SEPARATOR);
            SimpleDateFormat myDateFormat = localSimpleDateSeparator.get();
            return myDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 校验日期是否简单日期格式yyyyMMdd
     *
     * @param str 待校验字符串
     * @return 返回是否日期类型字符串
     */
    public static Boolean isSimpleDate(String str) {
        if(!str.matches(TipConstants.REG_NUM)){
            return false;
        }
        if(str.length()>8){
            return false;
        }
        return baseValidDate(str, YYYYMMDD_SIMPLE);
    }

    /**
     * 校验字符串是否规定的日期格式
     *
     * @param str 字符串
     * @return 返回是否规定的日期格式
     */
    private static Boolean baseValidDate(String str, String pattern) {
        Boolean result = true;
        SimpleDateFormat format = localSimpleDateSimple.get();
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            format.setLenient(false);
            format.parse(str);
        } catch (ParseException e) {
            result = false;
        }
        return result;
    }

    /**
     * 修改日期增减 天为单位
     *
     * @param date        日期
     * @param addOrReduce 需要增加或减少的天数
     * @return 返回增加或减少后的日期
     */
    public static Date modifyDate(Date date, Integer addOrReduce) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, addOrReduce);
        return calendar.getTime();
    }

    /**
     * 比较两个日期，如果第一个大于第二个返回true
     *
     * @param date1 第一个日期
     * @param date2 第二个日期
     * @return 如果第一个日期大于第二个返回true，否则返回false
     */
    public static Boolean compareDate(Date date1, Date date2) {
        return date1.after(date2);
    }


    /**
     * 计算endDate到startDate相差的天数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 相差的天数
     */
    public static Integer daysOfTwo(Date startDate, Date endDate) {
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.setTime(startDate);
        Integer day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
        aCalendar.setTime(endDate);
        Integer day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
        return day2 - day1;

    }
    /**
     * 日期和今天比较
     *
     * @param date
     * @return true date 大于今天，false date 等于或小于今天
     */
    public static boolean isDateAfterToday(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        boolean flag = false;
        try {
            String dateStr = formatter.format(date);
            String today = formatter.format(new Date());
            Date dateFormat = formatter.parse(dateStr);
            Date todayFormat = formatter.parse(today);
            int result = dateFormat.compareTo(todayFormat);
            if(result>0){
                return true;
            }else{
                return false;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return flag;
    }
    public static void main(String[] args) {
//        Date date1 = parseToDateWithShort("2016-03-01");//输入生产日期
//        Date date2 = parseToDateWithShort("2016-03-02");//系统生产日期
//        Date date3 = parseToDateWithShort("2016-03-21");//最小生产日期
//        Integer cha = 10;
//        System.out.println(daysOfTwo(date1,date2));

        System.out.println(parseSimpleStr("20160301"));

    }

}
