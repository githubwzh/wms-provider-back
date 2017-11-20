package com.womai.wms.rf.remote.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.stock.StockInfo;

import java.util.HashMap;

/**
 * ClassDescribe:库存
 * Author :wangzhanhua
 * Date: 2016-08-17
 * Since
 * To change this template use File | Settings | File Templates.
 */
public interface StockInfoRemoteService {
    /**
     * 分页查询库存信息
     * @param credentialsVO
     * @param map 必需参数：第几页，一页多少数据。可选参数，排序字段，排序规则，查询数据筛选条件
     * @return 如果有异常,查询结果无数据，或者参数不对返回RemoteResult.success 为false
     */
    RemoteResult<PageModel<StockInfo>> queryStockInfoPageList(CredentialsVO credentialsVO,HashMap<String,Object> map);

    /**
     * 冻结
     * @param credentialsVO 登陆信息
     * @param stockInfo  库存：库存id，原因id，原因内容，冻结数量bu
     * @param b
     * @return 冻结操作是否成功
     */
    RemoteResult<Boolean> freezeStockInfo(CredentialsVO credentialsVO, StockInfo stockInfo, boolean b);

    /**
     * 解冻
     * @param credentialsVO
     * @param stockInfoPara 库存：库存id，冻结数量bu
     * @return 解冻操作是否成功
     */
    RemoteResult<Boolean> unfreezeStockInfo(CredentialsVO credentialsVO, StockInfo stockInfoPara);
}
