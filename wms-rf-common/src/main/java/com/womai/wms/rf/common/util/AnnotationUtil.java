package com.womai.wms.rf.common.util;

import com.google.common.collect.Lists;
import com.womai.wms.rf.common.annotation.Receiver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 注解工具类，获取receiver注解的字段及colTip值
 * User:zhangwei
 * Date: 2016-05-25
 * To change this template use File | Settings | File Templates.
 */
public class AnnotationUtil {

    /**
     * 获取对象中Receiver注解的字段对象
     *
     * @param objectClass 类对象
     * @return 返回字段对象集合
     */
    public static List<FieldObject> getObjectFields(Class objectClass) {
        List<FieldObject> result = Lists.newArrayList();
        Field[] fields = objectClass.getDeclaredFields();
        FieldObject fieldObject;
        for (Field field : fields) {
            Annotation annotation = field.getAnnotation(Receiver.class);
            if (annotation != null) {
                fieldObject = new FieldObject();
                fieldObject.setFieldName(field.getName());
                Receiver receiver = (Receiver) annotation;
                fieldObject.setFieldTip(receiver.colTip());
                fieldObject.setCursorDown(receiver.cursorDown());
                fieldObject.setTopTip(receiver.topTip());
                fieldObject.setCanNull(receiver.canNull());
                fieldObject.setEncrypt(receiver.encrypt());
                result.add(fieldObject);
            }
        }
        return result;
    }

    /**
     * 按照字段名称获取注解过的字段对象
     *
     * @param objectClass 类对象
     * @param fieldNameList   所需的字段名称列表
     * @return 返回字段对象集合
     */
    public static List<FieldObject> getObjectFieldsByList(Class objectClass, List<String> fieldNameList) {
        List<FieldObject> result = Lists.newArrayList();
        Field[] fields = objectClass.getDeclaredFields();
        FieldObject fieldObject;
        for (Field field : fields) {
            Annotation annotation = field.getAnnotation(Receiver.class);
            if (annotation != null && fieldNameList.contains(field.getName())) {
                fieldObject = new FieldObject();
                fieldObject.setFieldName(field.getName());
                Receiver receiver = (Receiver) annotation;
                fieldObject.setFieldTip(receiver.colTip());
                fieldObject.setCursorDown(receiver.cursorDown());
                fieldObject.setTopTip(receiver.topTip());
                fieldObject.setCanNull(receiver.canNull());
                fieldObject.setEncrypt(receiver.encrypt());
                result.add(fieldObject);
            }
        }
        return result;
    }

}
