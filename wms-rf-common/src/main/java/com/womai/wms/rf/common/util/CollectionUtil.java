package com.womai.wms.rf.common.util;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 集合工具类.
 * User: 赵立伟
 * Date: 2015/1/15
 * Time: 14:53
 */
public abstract class CollectionUtil {
    /**
     * 判空
     * <ul>
     * <li>支持数组、List、Set、map等集合</li>
     * </ul>
     *
     * @param obj 对象
     * @return 为null或长度为0为真
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Object[]) {
            Object[] array = (Object[]) obj;
            return array.length <= 0;
        }
        if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            return collection.size() <= 0;
        }
        if (obj instanceof Map) {
            Map map = (Map) obj;
            return map.isEmpty();
        }
        return false;
    }

    /**
     * 判不为空
     * <ul>
     * <li>支持数组、List、Set、map等集合</li>
     * </ul>
     *
     * @param obj 对象
     * @return 不为null且长度不为0为真
     * @see #isEmpty(Object)
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 转换成数组
     * <ul>
     * <li>支持list、set等转换成数组</li>
     * </ul>
     *
     * @param collection 集合
     * @param eClass     元素类型
     * @param <E>
     * @return 数组
     */
    @SuppressWarnings(value = {"unchecked"})
    public static <E extends Object> E[] toArray(Collection<E> collection, Class<E> eClass) {
        if (isEmpty(collection)) {
            return (E[]) Array.newInstance(eClass, 0);
        }
        int size = collection.size();
        E[] array = (E[]) Array.newInstance(eClass, size);
        int index = 0;
        for (E value : collection) {
            array[index++] = value;
        }
        return array;
    }

    /**
     * 构建新的List
     *
     * @param obj 包含元素。如果不传，则构建空List
     * @return 新的List
     */
    public static List newList(Object... obj) {
        List result = new ArrayList();
        if (isEmpty(obj)) {
            return result;
        }
        for (Object o : obj) {
            result.add(o);
        }
        return result;
    }
    /**
     * 构建新的List带泛型
     *
     * @param values 包含元素。如果不传，则构建空List
     * @return 新的List
     */
    @SafeVarargs
    public static <E> List<E> newGenericList(E... values) {
        if (isEmpty(values)) {
            return new ArrayList<E>();
        }
        return new ArrayList<E>(Arrays.asList(values));
    }



    /**
     * List转换成String
     *
     * @param list      列表
     * @param separator 分隔符
     * @return 转换后的字符串
     */
    public static String list2Str(List list, String separator) {
        if (isEmpty(list)) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Object o = list.get(i);
            if (o == null) {
                continue;
            }
            if (i > 0) {
                str.append(separator);
            }
            str.append(String.valueOf(o));
        }
        return str.toString();
    }

    /**
     * 字符串按分隔符拆分后构建一个list
     *
     * @param str       字符串
     * @param separator 分隔符
     * @return list
     */
    public static List<String> str2List(String str, String separator) {
        List<String> list = new ArrayList<String>();
        if (StringUtils.isEmpty(str)) {
            return list;
        }
        String[] array = str.split(separator);
        for (String item : array) {
            if (StringUtils.isEmpty(item)) {
                list.add("");
            } else {
                list.add(item.trim());
            }
        }
        return list;
    }
}
