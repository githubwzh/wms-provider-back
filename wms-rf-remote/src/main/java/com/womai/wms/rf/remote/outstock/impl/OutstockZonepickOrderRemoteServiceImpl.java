package com.womai.wms.rf.remote.outstock.impl;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.outstock.OutstockZonepickOrderRemoteService;
import com.womai.zlwms.rfsoa.api.service.outstock.OutstockZonepickOrderService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockWarehouseGood;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockZoneworkorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ClassDescribe:四期拣货
 * Author :wangzhanhua
 * Date: 2017-04-21
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Service("outstockZonepickOrderRemoteService")
public class OutstockZonepickOrderRemoteServiceImpl implements OutstockZonepickOrderRemoteService {
    @Autowired
    private OutstockZonepickOrderService outstockZonepickOrderService ;
    @Override
    public RemoteResult<Map<String, Object>> applyOutstockZonepicks(CredentialsVO credentialsVO) {
        RemoteResult<Map<String, Object>> mapRemoteResult = outstockZonepickOrderService.applyOutstockZonepicks(credentialsVO);
        return mapRemoteResult;
    }

    @Override
    public RemoteResult<String> confirmZonePick(CredentialsVO credentialsVO, String containerno, OutstockWarehouseGood outstockWarehouseGood) {
        return outstockZonepickOrderService.confirmZonePick(credentialsVO, containerno,outstockWarehouseGood);
    }

    @Override
    public RemoteResult<Map<String, Object>> queryOutstockZonepicksStatusPicking(CredentialsVO credentialsVO) {
        return outstockZonepickOrderService.queryOutstockZonepicksStatusPicking(credentialsVO);
    }

    @Override
    public RemoteResult<String> scanContainer(CredentialsVO credentialsVO, String containerno, OutstockZoneworkorder outstockZoneworkorder) {
        return outstockZonepickOrderService.scanContainer(credentialsVO,containerno,outstockZoneworkorder);
    }

    @Override
    public RemoteResult<String> queryBaseContainerByWorksheetchildid(CredentialsVO credentialsVO, Long id) {
        return outstockZonepickOrderService.queryBaseContainerByWorksheetchildid(credentialsVO,id);
    }

    @Override
    public RemoteResult<String> onlyChangeContainer(CredentialsVO credentialsVO, String containerno, OutstockZoneworkorder outstockZoneworkorder) {
        return outstockZonepickOrderService.onlyChangeContainer(credentialsVO,containerno,outstockZoneworkorder);
    }

    @Override
    public RemoteResult<List<OutstockWarehouseGood>> queryOutstockWarehouseGoods(CredentialsVO credentialsVO, Long zoneworksheetid) {
        return outstockZonepickOrderService.queryOutstockWarehouseGoods(credentialsVO,zoneworksheetid);
    }

    @Override
    public RemoteResult<String> confirmZonePickAndChangeContainer(CredentialsVO credentialsVO, String containerno, OutstockWarehouseGood currOutstockWarehouseGood) {
        return outstockZonepickOrderService.confirmZonePickAndChangeContainer(credentialsVO,containerno,currOutstockWarehouseGood);
    }

    @Override
    public RemoteResult<String> confirmZonePickAndAllocation(CredentialsVO credentialsVO, String containerno, OutstockWarehouseGood currOutstockWarehouseGood) {
        return outstockZonepickOrderService.confirmZonePickAndAllocation(credentialsVO,containerno,currOutstockWarehouseGood);
    }
}
