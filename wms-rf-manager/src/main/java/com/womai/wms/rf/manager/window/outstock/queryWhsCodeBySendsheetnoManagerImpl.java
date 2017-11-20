package com.womai.wms.rf.manager.window.outstock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.DefaultKey;
import com.womai.wms.rf.common.constants.ErrorConstants;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.domain.outstock.OutstockPickup;
import com.womai.wms.rf.domain.outstock.QueryStorewhscode;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.outstock.OutstockPickupRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by keke on 17-4-25.
 * 集货管理-根据发货单查询集货库位
 */
@Scope("prototype")
@Component("queryWhsCodeBySendsheetnoManager")
public class queryWhsCodeBySendsheetnoManagerImpl extends ReceiveManager {
    private static final String WAREHOUSE_CODE = "realstorewhscode";//库位编码
    private static final String SENDSHEET_NO = "sendsheetno";//发货单号
    private ChannelHandlerContext ctx;
    private static final String NEXT_LOCATION = "nextLocation";//跳转的位置
    private static final int LOCATION_TO_CHANNELACTIVE = 0;//跳转到初始界面
    private static final String[] pageHeader = {Constants.BREAK_LINE, TipConstants.OUTSTOCK_PICKUP_STOREWHSCODE, Constants.SPLIT, ""};
    @Autowired
    private OutstockPickupRemoteService outstockPickupRemoteService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        //进入集周转箱页面
        super.initBaseMap(QueryStorewhscode.class, pageHeader, ctx);
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
        QueryStorewhscode queryStorewhscode = (QueryStorewhscode) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (SENDSHEET_NO.equals(lastCompleteColName)) {
            String sendsheetno = queryStorewhscode.getSendsheetno();
                RemoteResult<String> remoteResult = outstockPickupRemoteService.queryWhsCodeBySendsheetno(getCredentialsVO(ctx), sendsheetno);
                if (remoteResult.isSuccess()) {
                    if(StringUtils.isNotBlank(remoteResult.getT())){
                        HandlerUtil.clearAll(ctx.channel());
                        HandlerUtil.writer(ctx, pageHeader, 1, 1);
                        HandlerUtil.println(ctx, Constants.RF_MANAGER_REALWHSCODE + remoteResult.getT());
                        HandlerUtil.println(ctx, Constants.BREAK_LINE + ErrorConstants.ANY_KEY_CONTINUE);
                    }
                    accepterMap.put(NEXT_LOCATION, LOCATION_TO_CHANNELACTIVE);//跳转初始界面


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
