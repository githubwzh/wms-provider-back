package com.womai.wms.rf.remote.stock.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.stock.StockMoveRemoteService;
import com.womai.zlwms.rfsoa.api.service.stock.StockMoveOutService;
import com.womai.zlwms.rfsoa.api.service.stock.StockMoveService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.stock.StockMove;
import com.womai.zlwms.rfsoa.domain.stock.StockMoveItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe: 移位单主表实现类
 * Author :wangzhanhua
 * Date: 2016-08-18
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Service("stockMoveRemoteService")
public class StockMoveRemoteServiceImpl implements StockMoveRemoteService {
    @Autowired
    private StockMoveService stockMoveService;
    @Autowired
    private StockMoveOutService stockMoveOutService;

    /**
     * 查询当前用户，RF创建的未完成的移位单
     *
     * @param credentialsVO
     * @return 所有符合条件的移位单
     */

    @Override
    public List<StockMove> queryStockMovesUnfinished(CredentialsVO credentialsVO) {
        return stockMoveService.queryStockMovesUnfinished(credentialsVO).getT();
    }


    /**
     * 向创建的移位单里添加明细
     * 参数 stockMove 非空则先添加主单对象
     * @param credentialsVO
     * @param stockMoveItem 添加的移位单明细
     * @param stockMove 主单对象
     * @return 主单对象
     */
    @Override
        public RemoteResult<StockMove> saveStockMoveItem(CredentialsVO credentialsVO, StockMoveItem stockMoveItem, StockMove stockMove) {
        return stockMoveService.saveStockMoveItem(credentialsVO, stockMoveItem,stockMove);
    }


    @Override
    public RemoteResult<StockMove> getActiveNotMoveOutRFStockMoveByUser(CredentialsVO credentialsVo) {
        return stockMoveOutService.getActiveNotMoveOutRFStockMoveByUser(credentialsVo);
    }

    @Override
    public RemoteResult<Integer> confirmMoveOutAndReturnStatusCode(CredentialsVO credentialsVO, StockMove stockMove, String srcwhscode, Long skuid, String unitname, Date productiondate) {
        return stockMoveOutService.confirmMoveOut(credentialsVO, stockMove, srcwhscode, skuid, unitname, productiondate);
    }

    @Override
    public RemoteResult<Integer> confirmMoveOutRFCreate(CredentialsVO credentialsVO, StockMove stockMove) {
        return stockMoveOutService.confirmMoveOut(credentialsVO, stockMove, null, null, null, null);
    }

    /**
     * 激活移位单
     *
     * @param credentialsVO
     * @param stockMove
     * @return
     */
    @Override
    public RemoteResult<String> activateStockMove(CredentialsVO credentialsVO, StockMove stockMove) {
        return stockMoveService.activateStockMove(credentialsVO, stockMove);
    }

    @Override
    public RemoteResult<StockMove> validShelfCodeAndGetStockMoveByItForMoveOut(CredentialsVO credentialsVO, String shelfCode) {
        return stockMoveOutService.validShelfCodeAndGetStockMoveByItForMoveOut(credentialsVO, shelfCode);
    }

    @Override
    public RemoteResult<String> validSrcWhsCodeForMoveOut(CredentialsVO credentialsVO, StockMoveItem condition) {
        return stockMoveOutService.validSrcWhsCodeForMoveOut(credentialsVO, condition);
    }

    @Override
    public RemoteResult<List<Map<String, Object>>> queryMoveOutInfoGroupbyPkidAndProductionDate(CredentialsVO credentialsVo, StockMoveItem condition,String barCode) {
        return stockMoveOutService.queryMoveOutInfoGroupbyPkidAndProductionDate(credentialsVo, condition,barCode);
    }

    @Override
    public RemoteResult<List<Map<String, Object>>> queryMoveOutInfoGroupbyPkidAndProductionDate(CredentialsVO credentialsVo, StockMoveItem condition) {
        return stockMoveOutService.queryMoveOutInfoGroupbyPkidAndProductionDate(credentialsVo, condition,null);
    }

    /**
     * 查询登陆人RF创建，并且已经移出完成的移位单
     *
     * @param credentialsVO
     * @return
     */
    @Override
    public RemoteResult<StockMove> getStockMoveCreatedByRF(CredentialsVO credentialsVO) {
        return stockMoveService.getStockMoveCreatedByRF(credentialsVO);
    }

    /**
     * 根据移位单号，查询移位单
     * 如果是移位中状态的，必须同一操作人
     * @param credentialsVO
     * @param moveCode      移位单号
     * @return 移位单
     */
    @Override
    public RemoteResult<StockMove> queryStockMoveByCode(CredentialsVO credentialsVO, String moveCode) {
        return stockMoveService.queryStockMoveByCode(credentialsVO, moveCode);
    }

    /**
     * 分页获得移位单明细
     *
     * @param credentialsVO
     * @param map           查询明细的条件
     * @return 分页的明细列表
     */
    @Override
    public RemoteResult<PageModel<StockMoveItem>> queryStockMoveItemsPage(CredentialsVO credentialsVO, HashMap<String, Object> map) {
        return stockMoveService.queryStockMoveItemsPage(credentialsVO, map);
    }

    @Override
    public RemoteResult<Integer> countNotYetMoveOutBuByUnitAndProductionDate(CredentialsVO credentialsVO, StockMoveItem condition) {
        return stockMoveOutService.countNotYetMoveOutBuByUnitAndProductionDate(credentialsVO, condition);
    }

    /**
     * 移入
     *
     * @param currStockMove     移位主单
     * @param currStockMoveItem 移位明细
     * @return 更新version后的主单
     */
    @Override
    public RemoteResult<StockMove> processStockMoveIn(CredentialsVO credentialsVO, StockMove currStockMove, StockMoveItem currStockMoveItem) {
        return stockMoveService.processStockMoveIn(credentialsVO,currStockMove,currStockMoveItem);
    }

    /**
     * 根据主单id，查询所有明细
     *
     * @param credentialsVO
     * @param shelfid       主单id
     * @return 移位单明细
     */
    @Override
    public RemoteResult<List<StockMoveItem>> getStockMoveItemsByShelfid(CredentialsVO credentialsVO, Long shelfid) {
        return stockMoveService.getStockMoveItemsByShelfid(credentialsVO, shelfid);
    }

    @Override
    public RemoteResult<Integer> getCanMoveInBuSameSerialid(CredentialsVO credentialsVO, StockMoveItem currStockMoveItem) {
        return stockMoveService.getCanMoveInBuSameSerialid(credentialsVO,currStockMoveItem);
    }
}
