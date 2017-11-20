package com.womai.wms.rf.domain.stock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe:按照库位编码查询库存，接收参数对象
 * Author :wangzhanhua
 * Date: 2016-08-16
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class QueryStockInfoByWhsCode {
    @Receiver(colTip = "库位编码:",cursorDown = true,topTip = true)
    private String warehousecode;
    @Receiver(colTip = "请输入:",topTip = true)//B、N翻页
    private String selectStockInfoNo;
    public String getWarehousecode() {
        return warehousecode;
    }

    public void setWarehousecode(String warehousecode) {
        this.warehousecode = warehousecode;
    }

    public String getSelectStockInfoNo() {
        return selectStockInfoNo;
    }

    public void setSelectStockInfoNo(String selectStockInfoNo) {
        this.selectStockInfoNo = selectStockInfoNo;
    }

    @Override
    public String toString() {
        return "QueryStockInfoByWhsCode{" +
                "warehousecode='" + warehousecode + '\'' +
                ", selectStockInfoNo='" + selectStockInfoNo + '\'' +
                '}';
    }
}
