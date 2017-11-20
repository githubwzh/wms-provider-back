package com.womai.wms.rf.remote.outstock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockWarehouseGood;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockWorkorder;

import java.util.List;

/**
 * ClassDescribe:拣货管理
 * Author :wangzhanhua
 * Date: 2016-11-05
 * Since
 * To change this template use File | Settings | File Templates.
 */
public interface OutstockPickOrderRemoteService {
      /**
     * 根据拣货单号,查询拣货单，校验操作方式，状态，是否虚出单，校验通过返回根据库位，商品条码等分组拣货信息（）
     * @param credentialsVO 登陆信息
     * @param workSheetNo 拣货单号
     * @return 库位商品拣货信息
     */
    RemoteResult<List<OutstockWarehouseGood>> queryOutstockWarehouseGoodAfterValidateOrder(CredentialsVO credentialsVO, String workSheetNo);

    /**
     * 输入的数量BU等于待拣货数量BU
     * @param credentialsVO 登陆信息
     * @param currentWarehouseGoods 库位商品的拣货信息
     * @return 拣货确认处理结果
     */

    RemoteResult<String> confirmPick(CredentialsVO credentialsVO, OutstockWarehouseGood currentWarehouseGoods);

    /**
     * 校验该用户名是否在开关值中
     *
     * @param credentialsVO
     * @param username 用户名
     * @return 相应的开关值中如果有该用户名返回true,否则false
     */
    RemoteResult<Boolean> validateUsernameForPickAuthority(CredentialsVO credentialsVO, String username);

    /**
     * 拣货确认，重新分配
     * @param credentialsVO 登录信息
     * @param currentWarehouseGoods 拣货信息
     * @return 拣货确认，重新分配执行是否成功
     */
    RemoteResult<String> confirmAndReallocatePick(CredentialsVO credentialsVO, OutstockWarehouseGood currentWarehouseGoods);
}
