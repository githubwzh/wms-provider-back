package com.womai.wms.rf.remote.base.impl;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.base.GoodsinfoRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.api.service.base.BaseGoodsinfoService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BaseDictionary;
import com.womai.zlwms.rfsoa.domain.base.BaseGoodsinfo;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述:商品信息接口实现类
 * User:Qi Xiafei
 * Date: 2016-06-20
 * To change this template use File | Settings | File Templates.
 */
@Service("goodsInfoRemoteService")
public class GoodsinfoRemoteServiceImpl implements GoodsinfoRemoteService {

    @Autowired
    private BaseGoodsinfoService baseGoodsinfoService;

    @Override
    public RemoteResult<BaseGoodsinfo> getGoodsInfoByBarCode(CredentialsVO credentialsVO, String barcode) {
        return baseGoodsinfoService.getGoodsInfoByBarCode(credentialsVO, barcode);
    }

    @Override
    public BaseGoodsinfo getEnableGoodsByBarCode(CredentialsVO credentialsVO, String barcode) {
        RemoteResult<BaseGoodsinfo> remoteResult = baseGoodsinfoService.getGoodsInfoByBarCode(credentialsVO, barcode);
        if(!remoteResult.isSuccess()){
            return null;
        }
        BaseGoodsinfo goodsInfo = remoteResult.getT();
        if(goodsInfo.getStatus().equals(WmsConstants.STATUS_DISABLE)){
            return null;
        }
        return goodsInfo;
    }

    @Override
    public RemoteResult<Integer> updateColValuableFlagBySkuId(CredentialsVO credentialsVO, BaseGoodsinfo baseGoodsinfo) {
       return baseGoodsinfoService.updateColValuableFlagBySkuId(credentialsVO, baseGoodsinfo);
    }

    @Override
    public RemoteResult<Integer> updateColBatchRuleBySkuId(CredentialsVO credentialsVO, BaseGoodsinfo baseGoodsinfo) {
       return baseGoodsinfoService.updateColBatchRuleBySkuId(credentialsVO,baseGoodsinfo);
    }

    @Override
    public List<BaseDictionary> getShelfFlagList(CredentialsVO credentialsVO) {
        return baseGoodsinfoService.getShelfFlagList(credentialsVO);
    }

    @Override
    public RemoteResult<Integer> updateShelfFlagBySkuId(CredentialsVO credentialsVO, BaseGoodsinfo forUpdate) {
        return baseGoodsinfoService.updateShelfFlagBySkuId(credentialsVO,forUpdate);
    }

}
