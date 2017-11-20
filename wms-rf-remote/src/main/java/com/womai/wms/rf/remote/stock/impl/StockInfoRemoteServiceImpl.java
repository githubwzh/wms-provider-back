package com.womai.wms.rf.remote.stock.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.stock.StockInfoRemoteService;
import com.womai.zlwms.rfsoa.api.service.stock.StockInfoService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.stock.StockInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * ClassDescribe:
 * Author :wangzhanhua
 * Date: 2016-08-17
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Service("stockInfoRemoteService")
public class StockInfoRemoteServiceImpl implements StockInfoRemoteService {
    @Autowired
    private StockInfoService stockInfoService ;
    /**
     * 分页查询库存信息
     *
     * @param credentialsVO
     * @param map           必需参数：第几页，一页多少数据。可选参数，排序字段，排序规则，查询数据筛选条件
     * @return 如果有异常, 查询结果无数据，或者参数不对返回RemoteResult.success 为false
     */
    @Override
    public RemoteResult<PageModel<StockInfo>> queryStockInfoPageList(CredentialsVO credentialsVO, HashMap<String, Object> map) {
        return stockInfoService.queryStockInfoPageList(credentialsVO,map);
    }

    /**
     * 冻结
     *
     * @param credentialsVO 登陆信息
     * @param stockInfo     库存：库存id，原因id，原因内容，冻结数量bu
     * @param validateIC 是否校验ic在途
     * @return
     */
    @Override
    public RemoteResult<Boolean> freezeStockInfo(CredentialsVO credentialsVO, StockInfo stockInfo, boolean validateIC) {
        return stockInfoService.freezeStockInfo(credentialsVO,stockInfo,validateIC);
    }

    /**
     * 解冻
     *
     * @param credentialsVO
     * @param stockInfoPara 库存：库存id，冻结数量bu
     * @return 解冻操作是否成功
     */
    @Override
    public RemoteResult<Boolean> unfreezeStockInfo(CredentialsVO credentialsVO, StockInfo stockInfoPara) {
        return stockInfoService.unfreezeStockInfo(credentialsVO, stockInfoPara);
    }
}
