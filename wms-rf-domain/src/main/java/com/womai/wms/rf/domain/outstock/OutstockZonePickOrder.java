package com.womai.wms.rf.domain.outstock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe:拣货管理
 * Author :wangzhanhua
 * Date: 2016-11-05
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class OutstockZonePickOrder {
    @Receiver(colTip = "确认申请拣货（请输入1）:", topTip = true)
    private String applyPickOrder;
    @Receiver(colTip = "周转箱号:", topTip = true)
    private String containerno;
    @Receiver(colTip = "库位编码(换箱输入1):", topTip = true)
    private String warehouseCode;
    @Receiver(colTip = "商品条码:", topTip = true)
    private String barcode;
    @Receiver(colTip = "拣货数量BU:", topTip = true)
    private String pickBu;

    public String getApplyPickOrder() {
        return applyPickOrder;
    }

    public void setApplyPickOrder(String applyPickOrder) {
        this.applyPickOrder = applyPickOrder;
    }

    public String getContainerno() {
        return containerno;
    }

    public void setContainerno(String containerno) {
        this.containerno = containerno;
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

    public String getPickBu() {
        return pickBu;
    }

    public void setPickBu(String pickBu) {
        this.pickBu = pickBu;
    }

    @Override
    public String toString() {
        return "OutstockZonePickOrder{" +
                "applyPickOrder='" + applyPickOrder + '\'' +
                ", containerno='" + containerno + '\'' +
                ", warehouseCode='" + warehouseCode + '\'' +
                ", barcode='" + barcode + '\'' +
                ", pickBu='" + pickBu + '\'' +
                '}';
    }
}
