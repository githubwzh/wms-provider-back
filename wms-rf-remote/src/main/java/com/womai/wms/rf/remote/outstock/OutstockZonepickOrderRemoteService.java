package com.womai.wms.rf.remote.outstock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.domain.outstock.OutstockZonePickOrder;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockWarehouseGood;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockZonepick;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockZoneworkorder;

import java.util.List;
import java.util.Map;

/**
 * ClassDescribe:四期拣货管理
 * Author :wangzhanhua
 * Date: 2017-04-21
 * Since
 * To change this template use File | Settings | File Templates.
 */
public interface OutstockZonepickOrderRemoteService {
    /**
     * 根据登录人，申请所在区域的拣货任务，校验操作方式，状态，是否虚出单，校验通过返回根据库位，商品条码等分组拣货信息
     * @param credentialsVO 登陆信息
     * @return 库位商品拣货信息
     */
    RemoteResult<Map<String,Object>> applyOutstockZonepicks(CredentialsVO credentialsVO);

    /**
     *
     * @param credentialsVO
     * @param containerno
     *@param currOutstockZonepick  @return
     */
    RemoteResult<String> confirmZonePick(CredentialsVO credentialsVO, String containerno, OutstockWarehouseGood currOutstockZonepick);

    /**
     * 查询当前登录人正在拣货的任务
     * @param credentialsVO
     * @return
     */
    RemoteResult<Map<String,Object>> queryOutstockZonepicksStatusPicking(CredentialsVO credentialsVO);

    /**
     * 扫描周转箱
     * @param credentialsVO
     * @param containerno 周转箱号
     *@param outstockZoneworkorder 拣货子单
     *@return
     */
    RemoteResult<String> scanContainer(CredentialsVO credentialsVO, String containerno, OutstockZoneworkorder outstockZoneworkorder);

    /**
     * 根据拣货子单id查询拣货中的周转箱
     * @param credentialsVO
     * @param id
     * @return
     */
    RemoteResult<String> queryBaseContainerByWorksheetchildid(CredentialsVO credentialsVO, Long id);

    /**
     * 扫描库位的地方输入“1”，进行换箱
     * @param credentialsVO
     * @param containerno 周转箱号
     * @param outstockZoneworkorder 拣货子单
     * @return
     */
    RemoteResult<String> onlyChangeContainer(CredentialsVO credentialsVO, String containerno, OutstockZoneworkorder outstockZoneworkorder);

    /**
     * 输入数量bu的地方“0*”，需要拆分拣货明细，所以需要重新查询，拆分后的拣货明细
     * @param credentialsVO
     * @param zoneworksheetid 拣货子单
     * @return
     */
    RemoteResult<List<OutstockWarehouseGood>> queryOutstockWarehouseGoods(CredentialsVO credentialsVO, Long zoneworksheetid);

    /**
     * 拣货确认，换箱
     * @param credentialsVO
     * @param containerno
     *@param currOutstockWarehouseGood 拣货信息  @return
     */
    RemoteResult<String> confirmZonePickAndChangeContainer(CredentialsVO credentialsVO, String containerno, OutstockWarehouseGood currOutstockWarehouseGood);

    /**
     * 当前库位0个商品，或者小于计划数量bu
     * @param credentialsVO
     * @param containerno 周转箱号
     * @param currOutstockWarehouseGood 拣货信息
     * @return
     */
    RemoteResult<String> confirmZonePickAndAllocation(CredentialsVO credentialsVO, String containerno, OutstockWarehouseGood currOutstockWarehouseGood);
}
