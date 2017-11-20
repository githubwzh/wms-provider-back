package com.womai.wms.rf.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User:zhangwei
 * Date: 2016/5/17
 * To change this template use File | Settings | File Templates.
 */
public class ServiceLog {
    private static final Log SERVICE_LOG = LogFactory.getLog("service-error");

    public ServiceLog() {
    }

    /**
     * Degbu级别
     * @param youWant2See 待输入内容
     */
    public static void debugLog(String youWant2See) {
        if(SERVICE_LOG.isDebugEnabled()) {
            SERVICE_LOG.debug(youWant2See);
        }

    }

    /**
     * info级别
     * @param youWant2See 待输入内容
     */
    public static void infoLog(String youWant2See) {
        if(SERVICE_LOG.isInfoEnabled()) {
            SERVICE_LOG.info(youWant2See);
        }
    }

    /**
     * error级别
     * @param youWant2See 待输入内容
     */
    public static void errorLog(String youWant2See){
        if(SERVICE_LOG.isErrorEnabled()){
            SERVICE_LOG.error(youWant2See);
        }
    }
}
