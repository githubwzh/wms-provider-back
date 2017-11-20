package com.womai.wms.rf.domain.stock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe:解冻接收参数对象
 * Author :wangzhanhua
 * Date: 2016-09-26
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class UnfreezeStockInfo {
    @Receiver(colTip = "库位:",cursorDown = true,topTip = true)
    private String warehousecode;
    @Receiver(colTip = "商品条码:",cursorDown = true,topTip = true)
    private String barcode;
    @Receiver(colTip = "选择库存序号:",topTip = true)
    private String selectStockInfoNo;
    @Receiver(colTip = "请输入取消冻结数量BU:",topTip = true)
    private String stocknum;

    public String getWarehousecode() {
        return warehousecode;
    }

    public void setWarehousecode(String warehousecode) {
        this.warehousecode = warehousecode;
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

    public String getStocknum() {
        return stocknum;
    }

    public void setStocknum(String stocknum) {
        this.stocknum = stocknum;
    }

    @Override
    public String toString() {
        return "FreezeStockInfo{" +
                "warehousecode='" + warehousecode + '\'' +
                ", barcode='" + barcode + '\'' +
                ", selectStockInfoNo='" + selectStockInfoNo + '\'' +
                ", stocknum='" + stocknum + '\'' +
                '}';
    }
}
