package com.womai.wms.rf.domain.stock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe:RF移入接收参数对象
 * Author :wangzhanhua
 * Date: 2016-08-16
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class StockMoveIn {
    @Receiver(colTip = "移位单号:",cursorDown = true,topTip = true)
    private String moveOrderCode;
    @Receiver(colTip = "商品条码:",cursorDown = true,topTip = true)
    private String barcode;
    @Receiver(colTip = "选择序号:",topTip = true)
    private String selectItemNo;
    @Receiver(colTip = "扫描移入库位:",cursorDown = true,topTip = true)
    private String warehouseCode;
    @Receiver(colTip = "移入数量BU:",topTip = true)
    private String moveInBu;

    public String getMoveOrderCode() {
        return moveOrderCode;
    }

    public void setMoveOrderCode(String moveOrderCode) {
        this.moveOrderCode = moveOrderCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSelectItemNo() {
        return selectItemNo;
    }

    public void setSelectItemNo(String selectItemNo) {
        this.selectItemNo = selectItemNo;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getMoveInBu() {
        return moveInBu;
    }

    public void setMoveInBu(String moveInBu) {
        this.moveInBu = moveInBu;
    }

    @Override
    public String toString() {
        return "StockMoveIn{" +
                "moveOrderCode='" + moveOrderCode + '\'' +
                ", barcode='" + barcode + '\'' +
                ", selectItemNo='" + selectItemNo + '\'' +
                ", warehouseCode='" + warehouseCode + '\'' +
                ", moveInBu='" + moveInBu + '\'' +
                '}';
    }
}
