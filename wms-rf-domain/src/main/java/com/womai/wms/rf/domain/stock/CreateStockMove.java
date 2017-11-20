package com.womai.wms.rf.domain.stock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe:创建移位计划接收参数对象
 * Author :wangzhanhua
 * Date: 2016-08-16
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class CreateStockMove {
    @Receiver(colTip = "移位单类型:")
    private String orderType;
    @Receiver(colTip = "移出库位(激活请输0):",cursorDown = true,topTip = true)
    private String warehouseCode;
    @Receiver(colTip = "商品条码:",cursorDown = true,topTip = true)
    private String barcode;
    @Receiver(colTip = "选择序号:",topTip = true)
    private String selectStockInfoNo;
    @Receiver(colTip = "选择原因序号:",topTip = true)
    private String selectReasonNo;
    @Receiver(colTip = "移位数量BU:",topTip = true)
    private String planBu;
    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSelectStockInfoNo() {
        return selectStockInfoNo;
    }

    public void setSelectStockInfoNo(String selectStockInfoNo) {
        this.selectStockInfoNo = selectStockInfoNo;
    }

    public String getPlanBu() {
        return planBu;
    }

    public void setPlanBu(String planBu) {
        this.planBu = planBu;
    }

    public String getSelectReasonNo() {
        return selectReasonNo;
    }

    public void setSelectReasonNo(String selectReasonNo) {
        this.selectReasonNo = selectReasonNo;
    }

    @Override
    public String toString() {
        return "CreateStockMove{" +
                "orderType='" + orderType + '\'' +
                ", warehouseCode='" + warehouseCode + '\'' +
                ", barcode='" + barcode + '\'' +
                ", selectStockInfoNo='" + selectStockInfoNo + '\'' +
                ", planBu='" + planBu + '\'' +
                ", selectReasonNo='" + selectReasonNo + '\'' +
                '}';
    }
}
