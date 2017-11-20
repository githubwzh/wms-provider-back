package com.womai.wms.rf.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * logger调试日志输出
 * User: zhangwei
 * Date: 2016-05-06
 * To change this template use File | Settings | File Templates.
 */
public class ManagerLog {
    private static final Log MANAGER_LOG = LogFactory.getLog("manager-error");

    public ManagerLog() {
    }

    /**
     * Degbu级别
     * @param youWant2See 待输入内容
     */
    public static void debugLog(String youWant2See) {
        if(MANAGER_LOG.isDebugEnabled()) {
            MANAGER_LOG.debug(youWant2See);
        }

    }

    /**
     * info级别
     * @param youWant2See 待输入内容
     */
    public static void infoLog(String youWant2See) {
        if(MANAGER_LOG.isInfoEnabled()) {
            MANAGER_LOG.info(youWant2See);
        }
    }

    /**
     * error级别
     * @param youWant2See 待输入内容
     */
    public static void errorLog(String youWant2See){
        if(MANAGER_LOG.isErrorEnabled()){
            MANAGER_LOG.error(youWant2See);
        }
    }
    /**
     * error级别
     * @param youWant2See 待输入内容
     */
    public static void errorLog(String youWant2See,Throwable e){
        if(MANAGER_LOG.isErrorEnabled()){
            MANAGER_LOG.error(youWant2See,e);
        }
    }

}
