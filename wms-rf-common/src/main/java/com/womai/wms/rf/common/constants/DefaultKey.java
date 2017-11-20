package com.womai.wms.rf.common.constants;

import java.util.ArrayList;

/**
 *ReceiveManager使用的基本键及默认值
 * User:zhangwei
 * Date: 2016-05-18
 * To change this template use File | Settings | File Templates.
 */
public enum DefaultKey {

    curColName("curColName", ""),//当前接收中的字段，具体对象中的属性名称
    lastCompleteColName("lastCompleteColName", ""),//上一个接收完成的字段，具体对象中的属性名称
    completeSize("completeSize", 0),//已经接收完成的字段数量
    autoPrintNextCol("autoPrintNextCol", true),//是否自动打印下一个待接收字段
    rePage("rePage", false),//是否跳转页面
    clearDate("clearDate", false),//是否情况数据，重新初始化map
    curColErrMess("curColErrMess", ""),//当前回车后字段的校验结果
    objectClass("objectClass", new Object()),//需要接受数据的对象
    switchList("switchList", new ArrayList<String>()),//需要切换的列表
    listIndex("listIndex", 0),//切换列表时的序列号
    canMoveCursor("canMoveCursor", false);//是否可控制光标移动

    public String keyName;//键的名称
    public Object defaultVal;//默认名称

    DefaultKey(String keyName, Object defaultVal) {
        this.keyName = keyName;
        this.defaultVal = defaultVal;
    }

}
