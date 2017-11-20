package com.womai.wms.rf.remote.base;

import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;

import java.util.Map;

/**
 * ClassDescribe:
 * Author :wangzhanhua
 * Date: 2016-08-17
 * Since
 * To change this template use File | Settings | File Templates.
 */
public interface WarehouseInfoRemoteService {
    /**
     * 根据库位编码获得库位(生效)
     * @param credentialsVO
     * @param warehouseCode 库位编码
     * @return 库位信息
     */
    BaseWarehouseinfo getBaseWarehouseInfoByCode(CredentialsVO credentialsVO ,String warehouseCode);
    /**
     * 校验库位
     *
     * @param baseWarehouseinfo 库位信息
     * @param orderType              移位单类型
     * @return 校验通过true, 否则false
     */
    boolean validateWarehouse(BaseWarehouseinfo baseWarehouseinfo, Integer orderType);

    /**
     * 扫描移入库位时校验
     * @param ordertype 移位单类型
     * @param outWhstype 移出库位类型
     * @param inWhstype 移入库位类型
     * @param skuStatus 移出商品状态
     * @return
     */
     boolean validateWarehouseMoveIn(Integer ordertype,Integer outWhstype,Integer inWhstype,Integer skuStatus);

    /**
     *
     * @param credentialsVO
     * @param srcwhscode 原库位
     * @param warehouseCode 推荐库位
     * @return 原来库位对象和推荐库位对象
     */
    Map<String,BaseWarehouseinfo> getBaseWarehouseInfoByCodes(CredentialsVO credentialsVO, String srcwhscode, String warehouseCode);

    /**
     * 创建移位单时，根据移位单类型，移出库位类型，校验移出的商品状态
     * @param ordertype 移位单类型
     * @param outWhstype 移出库位类型
     * @param skuStatus 移出商品状态
     * @return 校验通过true，否则false
     */
    boolean validateSkuStatus(Integer ordertype, Integer outWhstype, Integer skuStatus);
}
