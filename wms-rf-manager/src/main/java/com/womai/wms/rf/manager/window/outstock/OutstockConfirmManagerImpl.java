package com.womai.wms.rf.manager.window.outstock;


import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.FieldObject;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.domain.outstock.ConfirmTransfer;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.outstock.TransOrderRemoteService;
import com.womai.zlwms.rfsoa.api.WMSErrorMess;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockTransorder;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 出库确认
 * Created by wzh on 16-5-8.
 */
@Scope("prototype")
@Component("outstockConfirmManager")
public class OutstockConfirmManagerImpl extends ReceiveManager {
    @Autowired
    private TransOrderRemoteService transOrderRemoteService;
    public final static String CONFIRMFLAG = "confirmFlag";
    public final static String TRANSCODE = "transferCode";
    public final static String NEXT_LOCATION = "next_location";
    private final static int CHANNEL_ACTIVE = 0;//跳转到初始的标识

    /**
     * 接收用户输入
     *
     * @param ctx    handler对象
     * @param object 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        Integer nextLocation = (Integer) accepterMap.get(NEXT_LOCATION);
        if (nextLocation == null) {//当有跳转标识时，不接收数据，任意键跳转业务流程
            receiveDataAndNotPrintNext(ctx, object, accepterMap);
        } else if (nextLocation == CHANNEL_ACTIVE) {
            channelActive(ctx);
            return;
        }
        ConfirmTransfer confirmTransfer = (ConfirmTransfer) getDataMap().get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (TRANSCODE.equals(lastCompleteColName)) {
            printBeforeNextField(Constants.RF_MANAGER_CONFIRM_YN, accepterMap, ctx);
            resetCurCol(CONFIRMFLAG, accepterMap, ctx);
        }
        if (CONFIRMFLAG.equals(lastCompleteColName)) {
            if (Constants.CONFIRM_Y.equalsIgnoreCase(confirmTransfer.getConfirmFlag())) {
                //处理业务逻辑
                OutstockTransorder outstockTransorder = new OutstockTransorder();
                String transsheetid = confirmTransfer.getTransferCode();//交接单号
                outstockTransorder.setTranssheetid(transsheetid);
                RemoteResult<String> remoteResult = transOrderRemoteService.confirmOutStock(getCredentialsVO(ctx), outstockTransorder);
                accepterMap.put(NEXT_LOCATION, 0);
                if (remoteResult.isSuccess()) {
                    colNeedReInput(CONFIRMFLAG, ErrorConstants.SUCCESS_CONTINUE, accepterMap, ctx, false);
                } else {
                    colNeedReInput(CONFIRMFLAG, remoteResult.getT() + ErrorConstants.TIP_TO_CONTINUE, accepterMap, ctx);
                }
            } else if (Constants.CANCEL_N.equalsIgnoreCase(confirmTransfer.getConfirmFlag())) {
                channelActive(ctx);//取消确认，跳转到初始界面
            } else {//输入错误
                colNeedReInput(CONFIRMFLAG, Constants.RF_MANAGER_ERROR_MSG_04, accepterMap, ctx);
            }
        }
        WMSDebugManager.debugLog("OutstockConfirmManagerImpl--Received:" + accepterMap);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 登录界面
        String[] pageHeader = {Constants.BREAK_LINE, Constants.RF_MANAGER_CONFIRM, Constants.SPLIT, ""};
        super.initBaseMap(ConfirmTransfer.class, pageHeader, ctx);
    }
}
