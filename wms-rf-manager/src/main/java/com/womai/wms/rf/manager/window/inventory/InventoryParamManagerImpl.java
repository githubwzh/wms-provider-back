package com.womai.wms.rf.manager.window.inventory;

import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.manager.util.ReceiveManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassDescribe:盘点业务处理
 * Author :zhangwei
 * Date: 2016-11-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(TipConstants.INVENTORY_PARAM_MANAGER)
public class InventoryParamManagerImpl extends ReceiveManager {

    public Map<String, Object> baseDataMap = new HashMap<String, Object>();//根据库位查询的基础数据

    public Map<String, Object> getBaseDataMap() {
        return baseDataMap;
    }

    public void setBaseDataMap(Map<String, Object> baseDataMap) {
        this.baseDataMap = baseDataMap;
    }

    public void clearBaseDataMap(){
        baseDataMap.clear();
    }
}



