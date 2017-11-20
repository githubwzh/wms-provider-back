package com.womai.wms.rf.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * logger调试日志输出
 * User: zhangwei
 * Date: 2016-05-06
 * To change this template use File | Settings | File Templates.
 */
public class WMSDebugManager {

    private static final Log DEBUG_LOG = LogFactory.getLog("WMS_DEBUG");

    public WMSDebugManager() {
    }

    /**
     * Degbu级别
     * @param youWant2See 待输入内容
     */
    public static void debugLog(String youWant2See) {
        if(DEBUG_LOG.isDebugEnabled()) {
            DEBUG_LOG.debug(youWant2See);
        }

    }

    public static void debugLog(String youWant2See,Throwable e) {
        if(DEBUG_LOG.isDebugEnabled()) {
            DEBUG_LOG.debug(youWant2See,e);
        }

    }

    /**
     * info级别
     * @param youWant2See 待输入内容
     */
    public static void infoLog(String youWant2See) {
        if(DEBUG_LOG.isInfoEnabled()) {
            DEBUG_LOG.info(youWant2See);
        }
    }

    /**
     * error级别
     * @param youWant2See 待输入内容
     */
    public static void errorLog(String youWant2See){
        if(DEBUG_LOG.isErrorEnabled()){
            DEBUG_LOG.error(youWant2See);
        }
    }

}
