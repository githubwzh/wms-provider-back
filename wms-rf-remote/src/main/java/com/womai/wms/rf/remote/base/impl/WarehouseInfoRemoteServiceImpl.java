package com.womai.wms.rf.remote.base.impl;

import com.womai.wms.rf.remote.base.WarehouseInfoRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.api.service.base.BaseWarehouseInfoService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BaseWarehouseinfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ClassDescribe:
 * Author :wangzhanhua
 * Date: 2016-08-17
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Service("warehouseInfoRemoteService")
public class WarehouseInfoRemoteServiceImpl implements WarehouseInfoRemoteService {
    @Autowired
    private BaseWarehouseInfoService baseWarehouseInfoService;

    /**
     * 根据库位编码获得库位（生效）
     *
     * @param credentialsVO
     * @param warehouseCode 库位编码
     * @return 库位信息
     */
    @Override
    public BaseWarehouseinfo getBaseWarehouseInfoByCode(CredentialsVO credentialsVO, String warehouseCode) {
        return baseWarehouseInfoService.getBaseWarehouseInfoByCode(credentialsVO, warehouseCode).getT();
    }

    /**
     * @param credentialsVO
     * @param srcwhscode    原库位
     * @param warehouseCode 推荐库位
     * @return 原来库位对象和推荐库位对象
     */
    @Override
    public Map<String, BaseWarehouseinfo> getBaseWarehouseInfoByCodes(CredentialsVO credentialsVO, String srcwhscode, String warehouseCode) {
        return baseWarehouseInfoService.getBaseWarehouseInfoByCodes(credentialsVO, srcwhscode, warehouseCode).getT();
    }

    /**
     * 校验库位(移出)
     *
     * @param baseWarehouseinfo 库位信息
     * @param orderType         移位单类型
     * @return 校验通过true, 否则false
     */
    @Override
    public boolean validateWarehouse(BaseWarehouseinfo baseWarehouseinfo, Integer orderType) {
        Integer warehouseType = baseWarehouseinfo.getWarehousetype();//库位类型
        if (WmsConstants.STOCKMOVE_ORDERTYPE_PUTONG == orderType) {
            // 普通移位：存货-次品-退拣
            return warehouseType == WmsConstants.WHSTYPE_CODE_CUNHUO || warehouseType == WmsConstants.WHSTYPE_CODE_CIPIN ||
                    warehouseType == WmsConstants.WHSTYPE_CODE_TUIJIAN || warehouseType == WmsConstants.WHSTYPE_CODE_XURUXUCHU;
        } else if (WmsConstants.STOCKMOVE_ORDERTYPE_CZZ == orderType) {
            // 残转正：次品
            return warehouseType == WmsConstants.WHSTYPE_CODE_CIPIN;
        } else if (WmsConstants.STOCKMOVE_ORDERTYPE_ZZC == orderType) {
            // 正转残：存货
            return warehouseType == WmsConstants.WHSTYPE_CODE_CUNHUO || warehouseType == WmsConstants.WHSTYPE_CODE_TUIJIAN ||
                    warehouseType == WmsConstants.WHSTYPE_CODE_XURUXUCHU;
        } else {
            return false;
        }
    }

    /**
     * @param ordertype  移位单类型
     * @param outWhstype 移出库位类型
     * @param inWhstype  移入库位类型
     * @param skuStatus  移出商品状态
     * @return
     */
    public boolean validateWarehouseMoveIn(Integer ordertype, Integer outWhstype, Integer inWhstype, Integer skuStatus) {
        switch (ordertype) {
            case WmsConstants.STOCKMOVE_ORDERTYPE_PUTONG: {//普通移位
                if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_CUNHUO)) {//存货--->存货,存货--->虚入虚出
                    if (inWhstype.equals(WmsConstants.WHSTYPE_CODE_XURUXUCHU) || inWhstype.equals(WmsConstants.WHSTYPE_CODE_CUNHUO)) {
                        if (skuStatus != WmsConstants.STOCK_GODDSSTATUS_NORMAL &&
                                skuStatus != WmsConstants.STOCK_GODDSSTATUS_FROZEN) { // 商品为正品或冻结
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_TUIJIAN) &&
                        (skuStatus == WmsConstants.STOCK_GODDSSTATUS_NORMAL ||
                        skuStatus == WmsConstants.STOCK_GODDSSTATUS_FROZEN)) {//退拣（正，冻）-->存货
                    if (!inWhstype.equals(WmsConstants.WHSTYPE_CODE_CUNHUO)) {
                        return false;
                    }
                } else if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_CIPIN)) {//次品
                    if (!inWhstype.equals(WmsConstants.WHSTYPE_CODE_CIPIN)) {
                        return false;
                    }
                } else if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_TUIJIAN) && skuStatus == WmsConstants.STOCK_GODDSSTATUS_BAD) {
                    if (!inWhstype.equals(WmsConstants.WHSTYPE_CODE_CIPIN)) {
                        return false;
                    }
                } else if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_XURUXUCHU)) {//虚入虚出--->存货 ，虚人虚出--->虚入虚出
                    if (!(inWhstype.equals(WmsConstants.WHSTYPE_CODE_CUNHUO) || inWhstype.equals(WmsConstants.WHSTYPE_CODE_XURUXUCHU))) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            break;
            case WmsConstants.STOCKMOVE_ORDERTYPE_ZZC: {//良转残
                if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_CUNHUO) && skuStatus == WmsConstants.STOCK_GODDSSTATUS_NORMAL) {//存货
                    if (!inWhstype.equals(WmsConstants.WHSTYPE_CODE_CIPIN)) {
                        return false;
                    }
                } else if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_TUIJIAN) && skuStatus == WmsConstants.STOCK_GODDSSTATUS_NORMAL) {
                    if (!inWhstype.equals(WmsConstants.WHSTYPE_CODE_CIPIN)) {
                        return false;
                    }
                } else if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_XURUXUCHU)) {//虚入虚出到次品
                    if (!inWhstype.equals(WmsConstants.WHSTYPE_CODE_CIPIN)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            break;
            case WmsConstants.STOCKMOVE_ORDERTYPE_CZZ: {//残转良
                if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_CIPIN)) {//次品-->存货,次品-->虚入虚出
                    if (!(inWhstype.equals(WmsConstants.WHSTYPE_CODE_CUNHUO) || inWhstype.equals(WmsConstants.WHSTYPE_CODE_XURUXUCHU))) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 创建移位单时，根据移位单类型，移出库位类型，校验移出的商品状态
     *
     * @param ordertype  移位单类型
     * @param outWhstype 移出库位类型
     * @param skuStatus  移出商品状态
     * @return 校验通过true，否则false
     */
    @Override
    public boolean validateSkuStatus(Integer ordertype, Integer outWhstype, Integer skuStatus) {
        switch (ordertype) {
            case WmsConstants.STOCKMOVE_ORDERTYPE_PUTONG: {//普通移位
                if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_CUNHUO)) {
                    if (skuStatus == WmsConstants.STOCK_GODDSSTATUS_NORMAL || skuStatus == WmsConstants.STOCK_GODDSSTATUS_FROZEN) { // 商品为正品或冻结
                        return true;
                    } else {
                        return false;
                    }
                } else if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_CIPIN)) {//移出次品库位时
                    if (skuStatus == WmsConstants.STOCK_GODDSSTATUS_BAD) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;//移出退拣库位时，可以是正残冻
                }
            }
            case WmsConstants.STOCKMOVE_ORDERTYPE_ZZC: {//良转残
                if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_CUNHUO) || outWhstype.equals(WmsConstants.WHSTYPE_CODE_TUIJIAN) ||
                        outWhstype.equals(WmsConstants.WHSTYPE_CODE_XURUXUCHU)) {//存货,退拣，虚入虚出，必须正品
                    if (skuStatus == WmsConstants.STOCK_GODDSSTATUS_NORMAL) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            case WmsConstants.STOCKMOVE_ORDERTYPE_CZZ: {//残转良
                if (outWhstype.equals(WmsConstants.WHSTYPE_CODE_CIPIN)) {//次品-->存货,次品-->虚入虚出
                    if (skuStatus == WmsConstants.STOCK_GODDSSTATUS_BAD) {
                        return true;
                    }else{
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}
