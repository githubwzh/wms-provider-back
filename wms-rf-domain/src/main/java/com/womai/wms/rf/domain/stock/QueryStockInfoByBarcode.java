package com.womai.wms.rf.domain.stock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe:按照商品条码查询库存，接收参数对象
 * Author :wangzhanhua
 * Date: 2016-08-16
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class QueryStockInfoByBarcode {
    @Receiver(colTip = "商品条码:",cursorDown = true,topTip = true)
    private String barcode;
    @Receiver(colTip = "请输入:",topTip = true)//B、N翻页
    private String selectStockInfoNo;

    public String getSelectStockInfoNo() {
        return selectStockInfoNo;
    }

    public void setSelectStockInfoNo(String selectStockInfoNo) {
        this.selectStockInfoNo = selectStockInfoNo;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    @Override
    public String toString() {
        return "QueryStockInfoByBarcode{" +
                "barcode='" + barcode + '\'' +
                ", selectStockInfoNo='" + selectStockInfoNo + '\'' +
                '}';
    }
}
