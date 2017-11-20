package com.womai.wms.rf.remote.stock.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.stock.ReplenishMoveInRemoteService;
import com.womai.zlwms.rfsoa.api.service.stock.ReplenishMoveInService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.stock.StockReplenishItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * ClassDescribe:RF补货移入Service接口实现类
 * Author :zhangwei
 * Date: 2016-10-08
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
@Service("replenishMoveInRemoteService")
public class ReplenishMoveInRemoteServiceImpl implements ReplenishMoveInRemoteService {

    @Autowired
    private ReplenishMoveInService replenishMoveInService;

    @Override
    public Long getReplenishByShelfCodeForIn(CredentialsVO credentialsVO, String shelfCode) {
        RemoteResult<Long> remoteResult = replenishMoveInService.getReplenishByShelfCodeForIn(credentialsVO, shelfCode);
        if (!remoteResult.isSuccess() || remoteResult.getT() == null || remoteResult.getT() == 0) {
            return 0L;
        }
        return remoteResult.getT();
    }

    @Override
    public PageModel<StockReplenishItem> queryReplenishItemPage(CredentialsVO credentialsVO, HashMap<String, Object> condition) {
        RemoteResult<PageModel<StockReplenishItem>> remoteResult = replenishMoveInService.queryReplenishItemPage(credentialsVO, condition);
        if (!remoteResult.isSuccess()) {
            return null;
        }
        return remoteResult.getT();
    }

    @Override
    public Boolean validateWareHouse(CredentialsVO credentialsVO, String whCode) {
        return replenishMoveInService.validateWareHouse(credentialsVO, whCode).getT();
    }

    @Override
    public Integer getRepUnMoveInYnBU(CredentialsVO credentialsVO, StockReplenishItem replenishItem) {
        return replenishMoveInService.getRepUnMoveInYnBU(credentialsVO, replenishItem).getT();
    }

    @Override
    public RemoteResult<Integer> confirmMoveIn(CredentialsVO credentialsVO, StockReplenishItem replenishItem, String whCode, Integer moveInBU) throws Exception {
        return replenishMoveInService.confirmMoveIn(credentialsVO, replenishItem, whCode, moveInBU);
    }
}
