package com.womai.wms.rf.remote.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.stock.StockMove;
import com.womai.zlwms.rfsoa.domain.stock.StockMoveItem;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:移位单接口
 * Author :wangzhanhua
 * Date: 2016-08-18
 * Since
 * To change this template use File | Settings | File Templates.
 */
public interface StockMoveRemoteService {
    /**
     * 查询当前用户，RF创建的未完成的移位单
     *
     * @param credentialsVO
     * @return 所有符合条件的移位单
     */

    List<StockMove> queryStockMovesUnfinished(CredentialsVO credentialsVO);

    /**
     * 向创建的移位单里添加明细
     * 参数stockMove 如果非空增先增加主单对象
     *
     * @param credentialsVO
     * @param stockMoveItem 添加的移位单明细
     * @param stockMove     主单对象
     * @return
     */
    RemoteResult<StockMove> saveStockMoveItem(CredentialsVO credentialsVO, StockMoveItem stockMoveItem, StockMove stockMove);

    /**
     * 查询由当前用户在RF创建的、激活状态的、未移出的、非报损的移位单
     *
     * @param credentialsVo 登录用户信息对象
     * @return 返回结果，查询到的移位信息
     */
    RemoteResult<StockMove> getActiveNotMoveOutRFStockMoveByUser(CredentialsVO credentialsVo);

    /**
     * 确认RF移出Web创建的移位单，并且返回移出单状态码
     *
     * @param credentialsVO  登录用户信息对象
     * @param stockMove      移位单信息
     * @param srcwhscode     原库位编码
     * @param skuid          商品id
     * @param unitname       单位名称
     * @param productiondate 生产日期
     * @return RemoteResult<Integer> 其中 Integer 代表状态码 ，resultCode 是错误提示
     * 1-移出后当前商品还有其他未移出的“单位/生产日期”
     * 2-移出后货位下还有其他商品
     * 3-移出后当前货位下全部移出，但是还存在其他货位未移出
     * 4-移出后当前移位单下全部移出
     */
    RemoteResult<Integer> confirmMoveOutAndReturnStatusCode(CredentialsVO credentialsVO, StockMove stockMove, String srcwhscode, Long skuid, String unitname, Date productiondate);

    /**
     * 确认RF移出RF创建的移位单
     *
     * @param credentialsVO 登录用户信息对象
     * @param stockMove     移位单信息
     * @return RemoteResult<Integer> 其中 Integer 对于RF创建的移位单无意义，resultCode 是错误提示
     */
    RemoteResult<Integer> confirmMoveOutRFCreate(CredentialsVO credentialsVO, StockMove stockMove);

    /**
     * 激活移位单
     *
     * @param credentialsVO
     * @param stockMove
     * @return
     */
    RemoteResult<String> activateStockMove(CredentialsVO credentialsVO, StockMove stockMove);


    /**
     * RF移出时验证移位单号正确性，若正确返回移位单信息
     *
     * @param credentialsVO 登录用户信息
     * @param shelfCode     移位单号
     * @return RemoteResult<StockMove> StockMove 移位单信息 resultCode 错误提示
     */
    RemoteResult<StockMove> validShelfCodeAndGetStockMoveByItForMoveOut(CredentialsVO credentialsVO, String shelfCode);

    /**
     * RF移出时验证库位编号正确性
     *
     * @param credentialsVO 登录用户信息
     * @param condition     查询条件
     * @return RemoteResult<String> String 打印在RF的信息
     */
    RemoteResult<String> validSrcWhsCodeForMoveOut(CredentialsVO credentialsVO, StockMoveItem condition);

    /**
     * 以移位单id、库位、商品条码作为查询条件，使用包装和生产日期分组，查询移出信息
     *
     * @param credentialsVo 登录用户信息
     * @param condition     查询条件
     * @param barCode       商品条码
     * @return RemoteResult<List<Map<String, Object>>>.getT()   List<Map<String,Object>> 移出信息列表，其中Map<String,Object>中包含包装单位，生产日期，未收货数量bu和商品id信息
     * ******** RemoteResult<List<Map<String, Object>>>.getResultCode() 存放错误信息
     */
    RemoteResult<List<Map<String, Object>>> queryMoveOutInfoGroupbyPkidAndProductionDate(CredentialsVO credentialsVo, StockMoveItem condition, String barCode);

    /**
     * 以移位单id、库位、商品id作为查询条件，使用包装和生产日期分组，查询移出信息
     *
     * @param credentialsVo 登录用户信息
     * @param condition     查询条件
     * @return RemoteResult<List<Map<String, Object>>>.getT()   List<Map<String,Object>> 移出信息列表，其中Map<String,Object>中包含包装单位，生产日期，未收货数量bu信息
     * ******** RemoteResult<List<Map<String, Object>>>.getResultCode() 存放错误信息
     */
    RemoteResult<List<Map<String, Object>>> queryMoveOutInfoGroupbyPkidAndProductionDate(CredentialsVO credentialsVo, StockMoveItem condition);

    /**
     * 查询登陆人RF创建，并且已经移出完成的移位单
     *
     * @param credentialsVO
     * @return
     */
    RemoteResult<StockMove> getStockMoveCreatedByRF(CredentialsVO credentialsVO);

    /**
     * 根据移位单号，查询移位单
     * 如果是移位中状态的，必须同一操作人
     * @param credentialsVO
     * @param moveCode      移位单号
     * @return 移位单
     */
    RemoteResult<StockMove> queryStockMoveByCode(CredentialsVO credentialsVO, String moveCode);

    /**
     * 分页获得移位单明细
     *
     * @param credentialsVO
     * @param map           查询明细的条件
     * @return 分页的明细列表
     */
    RemoteResult<PageModel<StockMoveItem>> queryStockMoveItemsPage(CredentialsVO credentialsVO, HashMap<String, Object> map);

    /**
     * RF移出选择生产日期和单位之后查询未移出bu数
     *
     * @param credentialsVO 登录用户信息
     * @param condition     查询条件
     * @return RemoteResult<Integer> Integer 未移出数 若有异常resultCode是错误提示
     */
    RemoteResult<Integer> countNotYetMoveOutBuByUnitAndProductionDate(CredentialsVO credentialsVO, StockMoveItem condition);

    /**
     * 移入
     *
     * @param currStockMove     移位主单
     * @param currStockMoveItem 移位明细
     * @return 更新version后的主单
     */
    RemoteResult<StockMove> processStockMoveIn(CredentialsVO credentialsVO, StockMove currStockMove, StockMoveItem currStockMoveItem);


    /**
     * 根据主单id，查询所有明细
     *
     * @param credentialsVO
     * @param shelfid       主单id
     * @return 移位单明细
     */
    RemoteResult<List<StockMoveItem>> getStockMoveItemsByShelfid(CredentialsVO credentialsVO, Long shelfid);

    /**
     * 获得这个明细的可移入数量bu
     * @param credentialsVO
     * @param currStockMoveItem 移位明细
     * @return
     */
    RemoteResult<Integer> getCanMoveInBuSameSerialid(CredentialsVO credentialsVO, StockMoveItem currStockMoveItem);
}
