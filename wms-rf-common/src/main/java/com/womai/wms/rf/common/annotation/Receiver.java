package com.womai.wms.rf.common.annotation;

import java.lang.annotation.*;

/**
 * 用于判断对象中需要接受用户输入的属性
 * User:zhangwei
 * Date: 2016-05-25
 * To change this template use File | Settings | File Templates.
 */
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Receiver {
    String colTip();//输入提示信息
    boolean cursorDown() default false;//光标位置是否需要在字段提示下边，false：右边；true:下边
    boolean topTip() default false;//错误提示是否显示在当前行的上边，false:字段提示的左边；true:字段提示的上边
    boolean canNull() default  false;//是否可空输入，直接回车，true:可直接回车
    String encrypt() default "";//需要显示的密文
}
