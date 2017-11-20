package com.womai.wms.rf.manager.window.inventory;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.CollectionUtil;
import com.womai.wms.rf.common.util.DateTimeUtil;
import com.womai.wms.rf.common.util.ManagerLog;
import com.womai.wms.rf.domain.inventory.Inventory;
import com.womai.wms.rf.domain.inventory.InventoryMP;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.inventory.InventoryRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryInfo;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:盘点业务处理
 * Author :zhangwei
 * Date: 2016-11-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(TipConstants.INVENTORY_URL_MANAGER)
public class InventoryManagerImpl extends ReceiveManager {
    @Autowired
    private InventoryRemoteService inventoryRemoteService;
    private final static String WH_CODE = "whCode";//库位编码
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.initBaseMap(Inventory.class, TipConstants.INVENTORY_PAGEHEADER, ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {

        Map<String, Object> accepterMap = getDataMap();
        receiveDataAndNotPrintNext(ctx, object, accepterMap);
        Inventory inventory = (Inventory) accepterMap.get(DefaultKey.objectClass.keyName);
        String lastCompleteColName = (String) accepterMap.get(DefaultKey.lastCompleteColName.keyName);
        if (WH_CODE.equals(lastCompleteColName)) {
            String whCode = inventory.getWhCode();
            RemoteResult<Map<String, Object>> remoteResult = inventoryRemoteService.queryInfoByWhCode(getCredentialsVO(ctx), whCode);
            if (!remoteResult.isSuccess()) {
                //如果按照库位查询不到主单及明细数据则重新输入库位
                colNeedReInput(WH_CODE, remoteResult.getResultCode(), accepterMap, ctx);
            } else {
                Map<String, Object> resultMap = remoteResult.getT();
                //将查询到的基本数据放到参数中
                InventoryParamManagerImpl inventoryParamManager = new InventoryParamManagerImpl();
                inventoryParamManager.setBaseDataMap(resultMap);
                ctx.pipeline().addAfter(Constants.ENCODE_HANDLER, TipConstants.INVENTORY_PARAM_MANAGER, inventoryParamManager);

                InventoryInfo inventoryInfo = (InventoryInfo) resultMap.get(WmsConstants.KEY_INVENTORY_INFO);
                if (WmsConstants.INVENTORY_TYPE_AP.equals(inventoryInfo.getInventorytype())) {
                    //进入的暗盘Handler
                    forward(TipConstants.INVENTORY_URL_AP_MANAGER, ctx);
                } else {
                    //进入的明盘Handler
                    forward(TipConstants.INVENTORY_URL_MP_MANAGER, ctx);
                }
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
