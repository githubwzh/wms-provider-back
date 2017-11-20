package com.womai.wms.rf.manager.window.outstock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.RFUtil;
import com.womai.wms.rf.domain.outstock.OutstockPickup;
import com.womai.wms.rf.domain.outstock.OutstockZonePickOrder;
import com.womai.wms.rf.domain.outstock.PreTransfer;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.outstock.OutstockPickupRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockProductorder;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by keke on 17-4-25.
 * 集货管理-集周转箱
 */
@Scope("prototype")
@Component("outstockPickupContainerManager")
public class OutstockPickupContainerManagerImpl extends ReceiveManager {
    private static final String CONTAINER_NO = "containerno";//周转箱号
    private static final String WAREHOUSE_CODE = "realstorewhscode";//库位编码
    private static final String SENDSHEET_NO = "sendsheetno";//发货单号
    private String sendsheetno;//发货单号
    private String storewhscode;//推荐库位
    private String realstorewhscode;//实际库位
    private Boolean isFinished;//是否集货完成
    private OutstockProductorder outstockProductorder;
    private ChannelHandlerContext ctx;
    private static final String NEXT_LOCATION = "nextLocation";//跳转的位置
    private static final int LOCATION_TO_CHANNELACTIVE = 0;//跳转到初始界面
    private static final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.OUTSTOCK_PICKUP_MANAGER, Constants.SPLIT, ""};
    @Autowired
    private OutstockPickupRemoteService outstockPickupRemoteService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        //进入集周转箱页面
        super.initBaseMap(OutstockPickup.class, pageHeader, ctx);
    }
    /**
     * 接收用户输入
     *
     * @param ctx handler对象
     * @param msg 每次接收到的字节数据
     * @throws Exception 抛出异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Map<String, Object> accepterMap = getDataMap();
        Integer nextLocation = (Integer) accepterMap.get(NEXT_LOCATION);
        if (nextLocation == null) {//当有跳转标识时，不接收数据，任意键跳转业务流程
            receiveDataAndNotPrintNext(ctx, msg, accepterMap);
        } else if (nextLocation == LOCATION_TO_CHANNELACTIVE) {
            accepterMap.remove(NEXT_LOCATION);
            channelActive(ctx);
            return;
        }
        OutstockPickup outstockPickup = (OutstockPickup) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (CONTAINER_NO.equals(lastCompleteColName)) {
            String containerno = outstockPickup.getContainerno();
            RemoteResult<Map<String, Object>> remoteResult = outstockPickupRemoteService.getStorewhscode(getCredentialsVO(ctx), containerno);
            if (remoteResult !=null && remoteResult.isSuccess()) {
                Map<String, Object> resultMap = (Map<String, Object>)remoteResult.getT();
                String errorMsg = (String) resultMap.get(WmsConstants.KEY_ERROR_MSG);
                HandlerUtil.clearAll(ctx.channel());
                HandlerUtil.writer(ctx, pageHeader, 1, 1);
                if (StringUtils.isBlank(errorMsg)) {
                    //分别对应发货单号、推荐库位
                    sendsheetno = (String)resultMap.get("sendsheetno");
                    storewhscode = (String)resultMap.get("storewhscode");
                    realstorewhscode = (String)resultMap.get("realstorewhscode");
                    isFinished = (Boolean)resultMap.get("isFinished");
                    outstockProductorder = (OutstockProductorder)resultMap.get(WmsConstants.KEY_OUTSTOCK_PARAM_PRODUCTORDER);

                    if(isFinished != null && isFinished.booleanValue()){
                        HandlerUtil.println(ctx,"发货单：" + sendsheetno + "已经集周转箱完成" );
                        HandlerUtil.println(ctx,"集周转箱库位是：" + storewhscode);
                        HandlerUtil.println(ctx,"请扫描发货单释放集货库位");
                        resetCurCol(SENDSHEET_NO, accepterMap, ctx);
                    }else{
                        HandlerUtil.println(ctx, Constants.RF_MANAGER_CONTAINERNO + containerno);
                        HandlerUtil.println(ctx, Constants.RF_MANAGER_SENDSHEETNO + this.sendsheetno);
                        HandlerUtil.println(ctx, Constants.RF_MANAGER_WHSCODE + this.storewhscode);
                        resetCurCol(WAREHOUSE_CODE, accepterMap, ctx);
                    }

                }else{
                    HandlerUtil.println(ctx, errorMsg);
                    HandlerUtil.println(ctx, Constants.BREAK_LINE + ErrorConstants.ANY_KEY_CONTINUE);
                    HandlerUtil.errorBeep(ctx);
                    accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);
                    //resetCurCol(CONTAINER_NO, accepterMap, ctx);
                }


            } else {
                colNeedReInput(CONTAINER_NO, remoteResult.getResultCode(), accepterMap, ctx);
            }
        }
        if (WAREHOUSE_CODE.equals(lastCompleteColName)) {
            String containerno = outstockPickup.getContainerno();
            String warehouseCode = outstockPickup.getRealstorewhscode();
            if(StringUtils.isNotBlank(realstorewhscode) && !realstorewhscode.equals(warehouseCode)){
               colNeedReInput(WAREHOUSE_CODE, "集货库位和推荐库位不一致！", accepterMap, ctx);
            }else{
                RemoteResult<String> remoteResult = outstockPickupRemoteService.pickupContainer(getCredentialsVO(ctx),containerno,warehouseCode,outstockProductorder);
                if (remoteResult !=null && remoteResult.isSuccess()) {
                    if(StringUtils.isNotBlank(remoteResult.getT())){
                        HandlerUtil.clearAll(ctx.channel());
                        HandlerUtil.writer(ctx, pageHeader, 1, 1);
                        HandlerUtil.println(ctx,Constants.RF_MANAGER_CONTAINERNO + containerno);
                        HandlerUtil.println(ctx,Constants.BREAK_LINE + remoteResult.getT());
                        HandlerUtil.println(ctx,"集周转箱库位是：" + warehouseCode);
                        HandlerUtil.println(ctx,"请扫描发货单释放集货库位");
                        resetCurCol(SENDSHEET_NO, accepterMap, ctx);
                    }else{
                        channelActive(ctx);//跳转初始界面
                    }

                } else {
                    colNeedReInput(WAREHOUSE_CODE, remoteResult.getResultCode(), accepterMap, ctx);
                }
            }

        }
        if (SENDSHEET_NO.equals(lastCompleteColName)) {
            String sendsheetno = outstockPickup.getSendsheetno();
            RemoteResult<String> remoteResult = outstockPickupRemoteService.releaseStorewhscode(getCredentialsVO(ctx), sendsheetno,outstockProductorder);
            if (remoteResult !=null && remoteResult.isSuccess()) {
                if(StringUtils.isNotBlank(remoteResult.getT())){
                    HandlerUtil.println(ctx, Constants.RF_MANAGER_REALWHSCODE + remoteResult.getT());
                    HandlerUtil.println(ctx, Constants.BREAK_LINE + ErrorConstants.ANY_KEY_CONTINUE);
                    accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);
                    //resetCurCol(CONTAINER_NO, accepterMap, ctx);
                }

            } else {
                colNeedReInput(SENDSHEET_NO, remoteResult.getResultCode(), accepterMap, ctx);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
