package com.womai.wms.rf.remote.stock.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.stock.ReplenishMoveOutRemoteService;
import com.womai.zlwms.rfsoa.api.service.stock.ReplenishMoveOutService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.stock.StockReplenishItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * ClassDescribe: RF补货移出soa接口实现类
 * Author :Xiafei Qi
 * Date: 2016-09-28
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Service("replenishMoveOutRemoteService")
public class ReplenishMoveOutRemoteServiceImpl implements ReplenishMoveOutRemoteService {

    @Autowired
    ReplenishMoveOutService replenishMoveOutService;

    @Override
    public RemoteResult<Long> validShelfCodeAndReturnShelfId(CredentialsVO credentialsVO, String shelfCode) {
        return replenishMoveOutService.validShelfCodeAndReturnShelfId(credentialsVO, shelfCode);
    }

    @Override
    public RemoteResult<String> validSrcWhsCode(CredentialsVO credentialsVO, StockReplenishItem condition) {
        return replenishMoveOutService.validSrcWhsCode(credentialsVO, condition);
    }

    @Override
    public RemoteResult<PageModel<StockReplenishItem>> validBarCodeAndGetItemPage(CredentialsVO credentialsVO, HashMap<String, Object> condition, String barCode) {
        return replenishMoveOutService.validBarCodeAndGetItemPage(credentialsVO, condition, barCode);
    }

    @Override
    public RemoteResult<PageModel<StockReplenishItem>> getPage(CredentialsVO credentialsVO, HashMap<String, Object> condition) {
        return replenishMoveOutService.getPage(credentialsVO, condition);
    }

    @Override
    public RemoteResult<Integer> submitAndGetReturnCode(CredentialsVO credentialsVO, StockReplenishItem selectedStockReplenishItem, int moveoutBu) {
        return replenishMoveOutService.submitAndGetReturnCode(credentialsVO, selectedStockReplenishItem, moveoutBu);
    }
}
