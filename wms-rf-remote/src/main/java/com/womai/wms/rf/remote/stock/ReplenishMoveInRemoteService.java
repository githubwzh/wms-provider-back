package com.womai.wms.rf.remote.stock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.stock.StockReplenishItem;

import java.util.HashMap;

/**
 * ClassDescribe:RF补货移入Service接口
 * Author :zhangwei
 * Date: 2016-10-08
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public interface ReplenishMoveInRemoteService {

    /**
     * 按照补货单号查询当前用户可进行移入操作的补货单
     *
     * @param credentialsVO 基础数据对象
     * @param shelfCode     补货单号
     * @return 如果存在可操作数据则返回补货主单数据主键ID，否则返回空
     */
    Long getReplenishByShelfCodeForIn(CredentialsVO credentialsVO, String shelfCode);

    /**
     * 分页查询补货明细
     *
     * @param credentialsVO 基础数据对象
     * @param condition     查询条件，包含补货主单ID及skuid
     * @return 分页数据
     */
    PageModel<StockReplenishItem> queryReplenishItemPage(CredentialsVO credentialsVO, HashMap<String, Object> condition);

    /**
     * 补货移入校验库位，生效、拣货区库位
     *
     * @param credentialsVO 基础数据对象
     * @param whCode        库位编码
     * @return 校验通过返回 true，否则false
     */
    Boolean validateWareHouse(CredentialsVO credentialsVO, String whCode);

    /**
     * 按照主单ID及行号查询未进行移入的BU
     *
     * @param credentialsVO 基础数据对象
     * @param replenishItem 明细数据，包含主单ID及行号
     * @return 未移入的数量
     */
    Integer getRepUnMoveInYnBU(CredentialsVO credentialsVO, StockReplenishItem replenishItem);

    /**
     * 确认移入操作
     *
     * @param credentialsVO 基础数据对象
     * @param replenishItem 补货明细数据，包含明细id及主单id
     * @param whCode        移入库位编码
     * @param moveInBU      移入数量
     * @return 根据返回值的不同，前端界面跳转到不同的操作步骤
     * -1： 系统异常，提示系统内部错误，任意键回到初始界面
     * 0：整单补货完成，提示整单补货完成，任意键回到初始界面
     * 1：商品条码下的所有明细全都补货完成，重新扫描商品条码
     * 2：商品条码对应的批次补货完成，重新选择商品条码对应的其它补货明细数据。
     * 3：商品条码对应的本批次未完全移入，前端重新显示未移入数量后重新输入数量BU继续补货操作。
     */
    RemoteResult<Integer> confirmMoveIn(CredentialsVO credentialsVO, StockReplenishItem replenishItem, String whCode, Integer moveInBU) throws Exception;

}
