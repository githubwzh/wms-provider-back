package com.womai.wms.rf.manager.util;

/**
 * ClassDescribe: RF严重错误，直接退回到登录页面
 * Author :Xiafei Qi
 * Date: 2016-10-14
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class RFFatalException extends Exception{
    public RFFatalException(String msg){
        super(msg);
    }
}
