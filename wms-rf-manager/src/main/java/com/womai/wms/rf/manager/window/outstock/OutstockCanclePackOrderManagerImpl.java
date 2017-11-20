package com.womai.wms.rf.manager.window.outstock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.domain.outstock.OutstockCanclePackOrder;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.outstock.OutstockCanclePackOrderRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockPackInfo;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockProductorder;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by keke on 17-8-9.
 * RF退拣
 */
@Scope("prototype")
@Component("outstockCanclePackOrderManager")
public class OutstockCanclePackOrderManagerImpl extends ReceiveManager{
    private ChannelHandlerContext ctx;
    private static final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.OUTSTOCK_CANCLE_ORDER, Constants.SPLIT, ""};
    private static final String PACK_CODE = "packcode";//面单号
    private static final String IS_CONTINUE = "isContinue";//是否确认退拣
    private boolean isToMenu;//跳转到主菜单
    private OutstockProductorder outstockProductorder;
    private  OutstockPackInfo outstockPackInfo;
    private static final String NEXT_LOCATION = "nextLocation";//跳转的位置
    private static final int LOCATION_TO_CHANNELACTIVE = 0;//跳转到初始界面
    @Autowired
    private OutstockCanclePackOrderRemoteService outstockCanclePackOrderRemoteService;
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        //进入RF退拣页面
        super.initBaseMap(OutstockCanclePackOrder.class, pageHeader, ctx);
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
        OutstockCanclePackOrder outstockCanclePackOrder = (OutstockCanclePackOrder)accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (PACK_CODE.equals(lastCompleteColName)) {
            String packcode = outstockCanclePackOrder.getPackcode();
            RemoteResult<Map<String, Object>> remoteResult = outstockCanclePackOrderRemoteService.validateCanalePackOrderByPackcode(getCredentialsVO(ctx), packcode);
            if(remoteResult !=null && remoteResult.isSuccess()){
                Map<String, Object> resultMap = remoteResult.getT();
                String errorMsg = (String) resultMap.get(WmsConstants.KEY_ERROR_MSG);
                if (StringUtils.isEmpty(errorMsg)) {
                    outstockProductorder =(OutstockProductorder)resultMap.get(WmsConstants.KEY_OUTSTOCK_PARAM_PRODUCTORDER);
                    outstockPackInfo =(OutstockPackInfo)resultMap.get(WmsConstants.KEY_OUTSTOCK_PARAM_PACK_INFO);
                    if(null == outstockProductorder || null == outstockPackInfo){
                        colNeedReInput(PACK_CODE, remoteResult.getResultCode(), accepterMap, ctx);
                    }else{
                        resetCurCol(IS_CONTINUE,accepterMap,ctx);
                    }
                }else{
                    colNeedReInput(PACK_CODE, errorMsg, accepterMap, ctx);
                }

            }else{
                printMsessage(ctx,accepterMap,false,remoteResult.getResultCode());
            }
        }
        if (IS_CONTINUE.equals(lastCompleteColName)) {
            String isContinue = outstockCanclePackOrder.getIsContinue();
            if (Constants.CANCEL_N.equalsIgnoreCase(isContinue)) {
                channelActive(ctx);
            } else if (Constants.CONFIRM_Y.equalsIgnoreCase(isContinue)) {
                RemoteResult<Map<String, Object>> remoteResult = outstockCanclePackOrderRemoteService.confirmCanalePackOrder(getCredentialsVO(ctx), outstockProductorder,outstockPackInfo);
                if(remoteResult !=null && remoteResult.isSuccess()){
                    Map<String, Object> resultMap = remoteResult.getT();
                    Integer unCancleNum =  (Integer)resultMap.get("unCancleNum");
                    if(unCancleNum == 0){
                        printMsessage(ctx,accepterMap,true,"该发货单已经退拣完成");
                    }else{
                        printMsessage(ctx,accepterMap,true,"该发货单还有" + unCancleNum +"个包裹需要退拣");
                    }

                }else{
                    printMsessage(ctx,accepterMap,false,remoteResult.getResultCode());
                }

            } else {
                colNeedReInput(IS_CONTINUE, ErrorConstants.ONLY_YN, accepterMap, ctx);
            }
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    private void printMsessage(ChannelHandlerContext ctx,Map<String, Object> accepterMap,boolean success,String message){
        HandlerUtil.println(ctx, message);
        if(!success){
            HandlerUtil.errorBeep(ctx);
        }
        accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);
        HandlerUtil.println(ctx, Constants.BREAK_LINE + ErrorConstants.ANY_KEY_CONTINUE);
    }
}
