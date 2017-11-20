package com.womai.wms.rf.remote.outstock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockProductorder;

import java.util.Map;

/**
 * Created by keke on 17-4-25.
 * 集货管理
 */
public interface OutstockPickupRemoteService {
    /**
     * 获取推荐的集货库位，绑定发货单号
     * @param credentialsVO
     * @param containerno
     * @return
     */
    RemoteResult<Map<String, Object>> getStorewhscode(CredentialsVO credentialsVO, String containerno);
    /**
     * 集周转箱
     * @param credentialsVO
     * @param storewhscode
     * @return
     */
    RemoteResult<String> pickupContainer(CredentialsVO credentialsVO, String containerno,String storewhscode,OutstockProductorder outstockProductorder);
    /**
     * 释放集货库位
     *
     * @param credentialsVO
     * @param sendsheetno
     * @return
     */
    public RemoteResult<String> releaseStorewhscode (CredentialsVO credentialsVO, String sendsheetno,OutstockProductorder outstockProductorder);
    /**
     * 查询集货库位
     * @param credentialsVO
     * @param sendsheetno
     * @return
     */
    RemoteResult<String> queryWhsCodeBySendsheetno(CredentialsVO credentialsVO, String sendsheetno);

    /**
     * 查询发货单
     * @param credentialsVO
     * @param whscode
     * @return
     */
    RemoteResult<String> querySendsheetnoByWhsCode(CredentialsVO credentialsVO, String whscode);

}
