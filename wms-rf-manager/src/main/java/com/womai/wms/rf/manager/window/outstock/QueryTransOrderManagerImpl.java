package com.womai.wms.rf.manager.window.outstock;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.ManagerLog;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.domain.outstock.QueryTransOrder;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.outstock.TransOrderRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockTransorder;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 根据面单号，查询交接扫描人，交接单号，如果未交接不显示
 *
 * @author wangzhanhua
 * @version 1.0
 * @since 16-5-11 下午2:37
 */
@Scope("prototype")
@Component("queryTransOrderManager")
public class QueryTransOrderManagerImpl extends ReceiveManager {
    @Autowired
    private TransOrderRemoteService transOrderRemoteService;
    public final static String FLAG = "flag";

    /**
     * 接收面单号
     *
     * @param ctx    handler对象
     * @param object 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        if (accepterMap.get(FLAG) == null) {//初始化标识
            accepterMap.put(FLAG, true);
        }
        Integer receiveResult = 0;
        QueryTransOrder queryTransOrder = null;
        if ((Boolean) accepterMap.get(FLAG)) {
            receiveResult = receiveData(ctx, object, accepterMap);
            queryTransOrder = (QueryTransOrder) accepterMap.get(DefaultKey.objectClass.keyName);
            WMSDebugManager.debugLog(receiveResult + "=当前数据对象:" + accepterMap);
            if (receiveResult == RECEIVER_TYPE_FORWARD) {
                return;  //页面已经跳转
            }
        } else {
            channelActive(ctx);
        }
        if (receiveResult == RECEIVER_TYPE_FINISHED) {//接收数据完成

            RemoteResult<Map<String, Object>> remoteResult = transOrderRemoteService.QueryTransOrderByPackCode(getCredentialsVO(ctx),
                    queryTransOrder.getPackCode());
            if (remoteResult == null) {
                printErrorMessage(ctx, Constants.SYSTEM_ERROR, accepterMap);
                ManagerLog.errorLog(Constants.SYSTEM_ERROR + "面单号：" + queryTransOrder.getPackCode());//输出到log
            } else {
                Map<String, Object> map = remoteResult.getT();
                String errorMsg = (String) map.get(WmsConstants.KEY_ERROR_MSG);
                //错误提示
                if (StringUtils.isNotEmpty(errorMsg)) {
                    printErrorMessage(ctx, errorMsg, accepterMap);
                } else {
                    //显示交接单号，扫描人
                    OutstockTransorder outstockTransorder = (OutstockTransorder) map.get(WmsConstants.KEY_OUTSTOCK_PARAM_TRANSORDER);
                    printSuccessMessage(ctx, outstockTransorder, accepterMap);
                }
            }
        }
        WMSDebugManager.debugLog("QueryTransOrderManagerImpl--Received:" + accepterMap);//开发时，输出到控制台
    }

    /**
     * 输出交接单信息
     *
     * @param ctx
     * @param outstockTransorder 交接扫描人，交接单号
     * @param accepterMap
     */
    private void printSuccessMessage(ChannelHandlerContext ctx, OutstockTransorder outstockTransorder, Map<String, Object> accepterMap) {
        String transCode = outstockTransorder.getTranssheetid();
        String scannerName = outstockTransorder.getCreatorname();
        HandlerUtil.locateCursor(ctx.channel(), 7, 1);//定位光标
        HandlerUtil.println(ctx, Constants.RF_MANAGER_SCANNER_NAME + scannerName);
        HandlerUtil.println(ctx, Constants.RF_MANAGER_TRANSCODE + transCode);
        printMsgCom(ctx, accepterMap);
    }

    /**
     * 错误提示语
     *
     * @param ctx
     * @param msg         提示信息
     * @param accepterMap 存储数据的map
     */

    private void printErrorMessage(ChannelHandlerContext ctx, String msg, Map<String, Object> accepterMap) {
        HandlerUtil.locateCursor(ctx.channel(), 7, 1);//定位光标
        HandlerUtil.println(ctx, msg);
        HandlerUtil.errorBeep(ctx);
        printMsgCom(ctx, accepterMap);
    }

    /**
     * 提示语
     *
     * @param ctx
     * @param accepterMap 存储数据的map
     */
    private void printMsgCom(ChannelHandlerContext ctx, Map<String, Object> accepterMap) {
        accepterMap.put(FLAG, false);//标识， 再次接收数据，初始化页面
        HandlerUtil.println(ctx, Constants.RF_MANAGER_ERROR_MSG_02);
        HandlerUtil.print(ctx, Constants.RF_MANAGER_ERROR_MSG_03);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 查询界面
        String[] pageHeader = {Constants.BREAK_LINE, Constants.RF_MANAGER_QUERY, Constants.SPLIT, ""};
        super.initBaseMap(QueryTransOrder.class, pageHeader, ctx);
    }
}
