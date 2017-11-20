package com.womai.wms.rf.remote.workproduct.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.workproduct.ProductShelfRemoteService;
import com.womai.zlwms.rfsoa.api.service.workproduct.ProductShelfService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.workproduct.WorkProductInfo;
import com.womai.zlwms.rfsoa.domain.workproduct.WorkProductShelf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassDescribe: 加工上架远程调用接口实现类
 * Author :zhangwei
 * Date: 2017-03-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Service("productShelfRemoteService")
public class ProductShelfRemoteServiceImpl implements ProductShelfRemoteService {
    @Autowired
    private ProductShelfService productShelfService;


    @Override
    public RemoteResult<Map<String,Object>> getProductInfoByNO(CredentialsVO credentialsVO, String productNO) {
        return productShelfService.getProductInfoByNO(credentialsVO,productNO);
    }

    @Override
    public RemoteResult<WorkProductShelf> getProductShelfByPalletCode(CredentialsVO credentialsVO, WorkProductShelf workProductShelf) {
        return productShelfService.getProductShelfByPalletCode(credentialsVO,workProductShelf);
    }

    @Override
    public PageModel<WorkProductShelf> queryShelfDetailPageModel(CredentialsVO credentialsVO, HashMap<String, Object> queryMap) {
        return productShelfService.queryShelfDetailPageModel(credentialsVO,queryMap).getT();
    }

    @Override
    public int getShelfActualYnBU(CredentialsVO credentialsVO, WorkProductShelf workProductShelf) {
        return productShelfService.getProductShelfActualYnBU(credentialsVO,workProductShelf).getT();
    }

    @Override
    public RemoteResult<Integer> confirmShelf(CredentialsVO credentialsVO, WorkProductShelf workProductShelf, String whCode, Integer shelfNum, boolean isPalletShelf) {
        return productShelfService.confirmShelf(credentialsVO, workProductShelf, whCode, shelfNum, isPalletShelf);
    }
}
