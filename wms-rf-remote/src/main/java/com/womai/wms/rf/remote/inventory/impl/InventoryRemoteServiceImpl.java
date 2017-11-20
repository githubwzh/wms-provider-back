package com.womai.wms.rf.remote.inventory.impl;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.inventory.InventoryRemoteService;
import com.womai.zlwms.rfsoa.api.service.inventory.InventoryService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryInfo;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryItem;
import com.womai.zlwms.rfsoa.domain.inventory.InventoryRegistItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:
 * Author :zhangwei
 * Date: 2016-11-08
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Service("inventoryRemoteService")
public class InventoryRemoteServiceImpl implements InventoryRemoteService {
    @Autowired
    private InventoryService inventoryService;


    @Override
    public RemoteResult<List<InventoryRegistItem>> queryRegisterItem(CredentialsVO credentialsVO, InventoryRegistItem queryItem) {
        return inventoryService.queryRegisterItem(credentialsVO,queryItem);
    }

    @Override
    public RemoteResult<Map<String, Object>> queryInfoByWhCode(CredentialsVO credentialsVO, String whCode) {
        return inventoryService.queryInfoByWhCode(credentialsVO, whCode);
    }

    @Override
    public RemoteResult<Map<String, Object>> getOneUnRegisteredDataForMP(CredentialsVO credentialsVO, BaseWarehouseinfo wareHouseInfo, InventoryInfo inventoryInfo, InventoryItem inventoryItem) {
        return inventoryService.getOneUnRegisteredDataForMP(credentialsVO, wareHouseInfo, inventoryInfo, inventoryItem);
    }

    @Override
    public RemoteResult<Map<String, Object>> getGoodsByBarCodeForMP(CredentialsVO credentialsVO, String barCode, InventoryItem inventoryItem) throws Exception {
        return inventoryService.getGoodsByBarCodeForMP(credentialsVO, barCode, inventoryItem);
    }

    @Override
    public RemoteResult<Map<String, Object>> getGoodsByBarCodeForAP(CredentialsVO credentialsVO, String barCode, BaseWarehouseinfo baseWarehouseinfo, InventoryItem inventoryItem) {
        return inventoryService.getGoodsByBarCodeForAP(credentialsVO, barCode, baseWarehouseinfo, inventoryItem);
    }

    @Override
    public RemoteResult<Integer> confirmRegister(CredentialsVO credentialsVO, Integer operateType, InventoryRegistItem registerParam, InventoryItem inventoryItem, InventoryInfo inventoryInfo) {
        return inventoryService.confirmRegister(credentialsVO,operateType,registerParam,inventoryItem,inventoryInfo);
    }

    @Override
    public RemoteResult<Map<String, Object>> endInventory(CredentialsVO credentialsVO, InventoryItem inventoryItem) {
        return inventoryService.endInventory(credentialsVO, inventoryItem);
    }
}
