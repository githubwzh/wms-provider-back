package com.womai.wms.rf.remote.outstock.impl;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.outstock.OutstockPickupRemoteService;
import com.womai.zlwms.rfsoa.api.service.outstock.OutstockPickupService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockProductorder;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockZonepick;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by keke on 17-4-25.
 * 集货管理
 */
@Service("outstockPickupRemoteService")
public class OutstockPickupRemoteServiceImpl implements OutstockPickupRemoteService {
    @Autowired
    private OutstockPickupService outstockPickupService;
    /**
     * 获取推荐的集货库位，绑定发货单号
     *
     * @param credentialsVO
     * @param containerno
     * @return
     */
    @Override
    public RemoteResult<Map<String, Object>> getStorewhscode(CredentialsVO credentialsVO, String containerno) {
        return outstockPickupService.getStorewhscode(credentialsVO, containerno);
    }

    /**
     * 集周转箱
     *
     * @param credentialsVO
     * @param storewhscode
     * @return
     */
    @Override
    public RemoteResult<String> pickupContainer(CredentialsVO credentialsVO, String containerno,String storewhscode,OutstockProductorder outstockProductorder) {
        return outstockPickupService.pickupContainer(credentialsVO, containerno, storewhscode, outstockProductorder);
    }

    /**
     * 释放集货库位
     *
     * @param credentialsVO
     * @param sendsheetno
     * @return
     */
    @Override
    public RemoteResult<String> releaseStorewhscode(CredentialsVO credentialsVO, String sendsheetno,OutstockProductorder outstockProductorder) {
        return outstockPickupService.releaseStorewhscode(credentialsVO, sendsheetno,outstockProductorder);
    }

    /**
     * 查询集货库位
     *
     * @param credentialsVO
     * @param sendsheetno
     * @return
     */
    @Override
    public RemoteResult<String> queryWhsCodeBySendsheetno(CredentialsVO credentialsVO, String sendsheetno) {
        return outstockPickupService.queryWhsCodeBySendsheetno(credentialsVO, sendsheetno);
    }

    /**
     * 查询发货单
     *
     * @param credentialsVO
     * @param whscode
     * @return
     */
    @Override
    public RemoteResult<String> querySendsheetnoByWhsCode(CredentialsVO credentialsVO, String whscode) {
        return outstockPickupService.querySendsheetnoByWhsCode(credentialsVO, whscode);
    }
}
