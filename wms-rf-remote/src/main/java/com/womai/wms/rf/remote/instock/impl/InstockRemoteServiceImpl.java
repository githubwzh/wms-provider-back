package com.womai.wms.rf.remote.instock.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.instock.InstockRemoteService;
import com.womai.zlwms.rfsoa.api.service.instock.InstockService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BasePallet;
import com.womai.zlwms.rfsoa.domain.instock.Instock;
import com.womai.zlwms.rfsoa.domain.instock.InstockDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User:zhangwei
 * Date: 2016-06-24
 * To change this template use File | Settings | File Templates.
 */
@Service("instockRemoteService")
public class InstockRemoteServiceImpl implements InstockRemoteService {
    @Autowired
    private InstockService instockService;


    @Override
    public RemoteResult<Instock> getPurchaseByASNCode(CredentialsVO credentialsVO,String asnCode) {
        return instockService.getPurchaseByASNCode(credentialsVO,asnCode);
    }

    @Override
    public Boolean intentionIsOutStock(CredentialsVO credentialsVO, String serialNo) {
        RemoteResult<Boolean> remoteResult = instockService.intentionIsOutStock(credentialsVO,serialNo);
        if(remoteResult==null || !remoteResult.isSuccess() || remoteResult.getT()==null){
            return false;
        }
        return remoteResult.getT();
    }

    @Override
    public  RemoteResult<List<Instock>> queryIntentionByASNCodeOrPurchaseCode(CredentialsVO credentialsVO, String ASNOrPurchaseCode) {
        return instockService.queryIntentionByASNCodeOrPurchaseCode(credentialsVO, ASNOrPurchaseCode);
    }

    @Override
    public Map<String, Object> queryDetailAndGoodsAndPackaging(CredentialsVO credentialsVO, Long asnInStockId, String barCode) {
        RemoteResult<Map<String,Object>> remoteResult = instockService.queryDetailAndGoodsAndPackaging(credentialsVO, asnInStockId, barCode);
        if (remoteResult==null || !remoteResult.isSuccess() || remoteResult.getT()==null) {
            return null;
        }
        return remoteResult.getT();
    }

    @Override
    public Boolean existDuplicateDetail(CredentialsVO credentialsVO, InstockDetail instockDetail) {
        RemoteResult<Boolean> remoteResult = instockService.existDuplicateDetail(credentialsVO, instockDetail);
        if (remoteResult==null || !remoteResult.isSuccess() || remoteResult.getT()==null) {
            return false;
        }
        return remoteResult.getT();
    }

    @Override
    public List<InstockDetail> querySameSerialIDDetails(CredentialsVO credentialsVO, InstockDetail instockDetail) {
        RemoteResult<List<InstockDetail>> remoteResult = instockService.querySameSerialIDDetails(credentialsVO,instockDetail);
        if (remoteResult==null || !remoteResult.isSuccess() || remoteResult.getT()==null) {
            return null;
        }
        return remoteResult.getT();
    }

    @Override
    public BasePallet getPalletByCode(CredentialsVO credentialsVO, String palletCode) {
        RemoteResult<BasePallet> remoteResult = instockService.getPalletByCode(credentialsVO,palletCode);
        if (remoteResult==null || !remoteResult.isSuccess() || remoteResult.getT()==null) {
            return null;
        }
        return remoteResult.getT();
    }

    @Override
    public RemoteResult<String> confirmInStock(CredentialsVO credentialsVO, Instock instock, InstockDetail instockDetail,int[] orderType) {
        return instockService.confirmInStock(credentialsVO, instock, instockDetail,orderType);
    }

    @Override
    public RemoteResult<List<Instock>> getByASNCodeOrPurchaseNo(CredentialsVO credentialsVO,Instock instock) {
        return instockService.getByASNCodeOrPurchaseNo(credentialsVO,instock);
    }

    @Override
    public RemoteResult<List<Instock>> getIntentionListByASNCodeOrPurchaseNo(CredentialsVO credentialsVO, String key) {
        return instockService.getIntentionListByASNCodeOrPurchaseNo(credentialsVO,key);
    }

    @Override
    public RemoteResult<String> confirmInstockPosting(CredentialsVO credentialsVO, String asnInstockCode) {
        return instockService.confirmInstockPosting(credentialsVO,asnInstockCode);
    }

    @Override
    public RemoteResult<Instock> getByAsnCode(CredentialsVO credentialsVO, String asnInstockCode) {
        return instockService.getByAsnCode(credentialsVO,asnInstockCode);
    }

    @Override
    public RemoteResult<String> queryProDateInOutOrder(CredentialsVO credentialsVO, HashMap<String,Object> hashMap) {
        return instockService.queryProDateInOutOrder(credentialsVO,hashMap);
    }

    @Override
    public RemoteResult<PageModel<InstockDetail>> queryInstockDetailsPage(CredentialsVO credentialsVO, HashMap<String, Object> paraMap) {
        return instockService.queryInstockDetailsPage(credentialsVO,paraMap);
    }

    /**
     * 意向单快捷收货主业务处理
     *
     * @param credentialsVO
     * @param hashMap       instock    入库主单 instockDetail 入库单明细 orderType[]  需要处理的入库单类型 check_status ,reasonid ,remark
     * @return 返回处理结果，如果全部收货true，否则false
     */
    @Override
    public RemoteResult<Boolean> confirmInStockFast(CredentialsVO credentialsVO, Map<String, Object> hashMap) {
        return instockService.confirmInStockFast(credentialsVO,hashMap);
    }

    /**
     * 意向单快捷收货-过账
     *
     * @param credentialsVO
     * @param asnInstockCode ASN单号
     */
    @Override
    public RemoteResult<String> confirmInstockPostingAndCheckFast(CredentialsVO credentialsVO, String asnInstockCode) {
        return instockService.confirmInstockPostingAndCheckFast(credentialsVO, asnInstockCode);
    }

    @Override
    public RemoteResult<List<Instock>> queryIntentionByASNCodeOrPurchaseCodeFast(CredentialsVO credentialsVO, String scanCode) {
        return instockService.queryIntentionByASNCodeOrPurchaseCodeFast(credentialsVO,scanCode);
    }
}
