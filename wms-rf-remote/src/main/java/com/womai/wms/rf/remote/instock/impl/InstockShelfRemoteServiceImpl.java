package com.womai.wms.rf.remote.instock.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.instock.InstockShelfRemoteService;
import com.womai.zlwms.rfsoa.api.service.instock.InstockShelfOrderService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.instock.InstockShelforder;
import com.womai.zlwms.rfsoa.domain.instock.InstockShelforderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * ClassDescribe:上架主单远程调用接口实现类
 * Author :zhangwei
 * Date: 2016-08-17
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Service("instockShelfRemoteService ")
public class InstockShelfRemoteServiceImpl implements InstockShelfRemoteService {
    @Autowired
    private InstockShelfOrderService instockShelfOrderService;

    @Override
    public PageModel<InstockShelforder> queryShelfOrderPageModel(CredentialsVO credentialsVO, HashMap<String, Object> queryMap) {
        RemoteResult<PageModel<InstockShelforder>> remoteResult = instockShelfOrderService.queryShelfOrderPageModel(credentialsVO, queryMap);
        return remoteResult.getT();
    }

    @Override
    public List<InstockShelforderDetail> queryShelfDetailList(CredentialsVO credentialsVO, InstockShelforder instockShelforder) {
        RemoteResult<List<InstockShelforderDetail>> instockShelforderDetailList = instockShelfOrderService.queryShelfDetailList(credentialsVO, instockShelforder);
        return instockShelforderDetailList.getT();
    }


    @Override
    public PageModel<InstockShelforderDetail> queryShelfDetailPageModel(CredentialsVO credentialsVO, HashMap<String, Object> queryMap) {
        RemoteResult<PageModel<InstockShelforderDetail>> remoteResult = instockShelfOrderService.queryShelfDetailPageModel(credentialsVO, queryMap);
        return remoteResult.getT();

    }

    @Override
    public  RemoteResult<InstockShelforderDetail> getShelfDetailByPalletCode(CredentialsVO credentialsVO, String palletCode, Long shelfId) {
        return instockShelfOrderService.getShelfDetailByPalletCode(credentialsVO, palletCode, shelfId);
    }

    @Override
    public boolean checkWareHouseForShelf(CredentialsVO credentialsVO, String wareHouseNo, InstockShelforderDetail instockShelforderDetail, boolean isPalletShelf) {
        RemoteResult<Boolean> remoteResult = instockShelfOrderService.checkWareHouseForShelf(credentialsVO, wareHouseNo, instockShelforderDetail, isPalletShelf);
        return remoteResult.getT();
    }

    @Override
    public int getShelfActualYnBU(CredentialsVO credentialsVO, InstockShelforderDetail instockShelforderDetail) {
        RemoteResult<Integer> remoteResult = instockShelfOrderService.getShelfActualYnBU(credentialsVO, instockShelforderDetail);
        return remoteResult.getT();
    }

    @Override
    public RemoteResult<Integer> confirmShelf(CredentialsVO credentialsVO, InstockShelforderDetail instockShelforderDetail, String whCode, Integer shelfNum, boolean isPalletShelf) {
        return instockShelfOrderService.confirmShelf(credentialsVO, instockShelforderDetail, whCode, shelfNum, isPalletShelf);
    }

    @Override
    public RemoteResult<String> validateNeedConfirmDate(CredentialsVO credentialsVO) {
        return instockShelfOrderService.validateNeedConfirmDate(credentialsVO);
    }

    @Override
    public RemoteResult<Boolean> validateWareHouseForShelf(CredentialsVO credentialsVO, String scanWHCode, InstockShelforderDetail selectedShelfDetail, boolean isPalletShelf) {
        return instockShelfOrderService.validateWareHouseForShelf(credentialsVO, scanWHCode, selectedShelfDetail, isPalletShelf);
    }

    @Override
    public RemoteResult<Boolean> validateInstockShelforder(CredentialsVO credentialsVO, String orderCode) {
        return instockShelfOrderService.validateInstockShelforder(credentialsVO, orderCode);
    }

    @Override
    public RemoteResult<Boolean> validateBaseGoodsShelfOrder(CredentialsVO credentialsVO, Long shelfid, String barCode) {
        return instockShelfOrderService.validateBaseGoodsShelfOrder(credentialsVO, shelfid, barCode);
    }

    @Override
    public RemoteResult<String> validateAutoShowShelfCode(CredentialsVO credentialsVO) {
        return instockShelfOrderService.validateAutoShowShelfCode(credentialsVO);
    }

    @Override
    public RemoteResult<String> queryShelferNameFromRFLog(CredentialsVO credentialsVO, String orderCode) {
        return instockShelfOrderService.queryShelferNameFromRFLog(credentialsVO,orderCode);
    }
}
