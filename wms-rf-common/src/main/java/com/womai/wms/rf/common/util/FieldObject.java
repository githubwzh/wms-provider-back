package com.womai.wms.rf.common.util;

/**
 * 字段属性对象
 * User:zhangwei
 * Date: 2016-05-25
 * To change this template use File | Settings | File Templates.
 */
public class FieldObject {

    private String fieldName;//对象的属性名
    private String fieldTip;//对象属性注解上的输入提示
    private boolean cursorDown;//光标是否需要在字段提示的下边
    private boolean topTip;//错误提示是否显示在当前行的上边，默认在左边
    private boolean canNull;//是否可空输入，直接回车
    private String encrypt;//显示的密文

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldTip() {
        return fieldTip;
    }

    public void setFieldTip(String fieldTip) {
        this.fieldTip = fieldTip;
    }

    public boolean isCursorDown() {
        return cursorDown;
    }

    public void setCursorDown(boolean cursorDown) {
        this.cursorDown = cursorDown;
    }

    public boolean isTopTip() {
        return topTip;
    }

    public void setTopTip(boolean topTip) {
        this.topTip = topTip;
    }

    public boolean isCanNull() {
        return canNull;
    }

    public void setCanNull(boolean canNull) {
        this.canNull = canNull;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    @Override
    public String toString() {
        return "FieldObject{" +
                "fieldName='" + fieldName + '\'' +
                ", fieldTip='" + fieldTip + '\'' +
                ", cursorDown=" + cursorDown +
                ", topTip=" + topTip +
                ", canNull=" + canNull +
                ", encrypt='" + encrypt + '\'' +
                '}';
    }
}
