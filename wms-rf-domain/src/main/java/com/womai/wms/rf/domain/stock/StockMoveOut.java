package com.womai.wms.rf.domain.stock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe: RF移出业务domain
 * Author :Xiafei Qi
 * Date: 2016-08-19
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class StockMoveOut {
    @Receiver(colTip = "输入y确认全部移出:", topTip = true)
    private String confirmMoveAll;// 确认全部移出，如果用户有未完成的RF移位任务直接显示该字段

    @Receiver(colTip = "移位单号：", topTip = true)
    private String shelfcode;// 移位单号

    @Receiver(colTip = "移出库位：", topTip = true)
    private String srcWhsCode;// 原库位编码

    @Receiver(colTip = "商品条码：", topTip = true)
    private String barCode; // 商品条码

    @Receiver(colTip="请选择单位：",topTip=true)
    private String selectUnit; //选择单位

    @Receiver(colTip = "确认明细全部移出(Y/N)：",topTip = true)
    private String confirmMoveAllDetail;// 确认明细全部移出

    @Override
    public String toString() {
        return "StockMoveOut{" +
                "confirmMoveAll='" + confirmMoveAll + '\'' +
                ", shelfcode='" + shelfcode + '\'' +
                ", srcWhsCode='" + srcWhsCode + '\'' +
                ", barCode='" + barCode + '\'' +
                ", selectUnit='" + selectUnit + '\'' +
                ", confirmMoveAllDetail='" + confirmMoveAllDetail + '\'' +
                '}';
    }

    public String getConfirmMoveAll() {
        return confirmMoveAll;
    }

    public void setConfirmMoveAll(String confirmMoveAll) {
        this.confirmMoveAll = confirmMoveAll;
    }

    public String getShelfcode() {
        return shelfcode;
    }

    public void setShelfcode(String shelfcode) {
        this.shelfcode = shelfcode;
    }

    public String getSrcWhsCode() {
        return srcWhsCode;
    }

    public void setSrcWhsCode(String srcWhsCode) {
        this.srcWhsCode = srcWhsCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getSelectUnit() {
        return selectUnit;
    }

    public void setSelectUnit(String selectUnit) {
        this.selectUnit = selectUnit;
    }

    public String getConfirmMoveAllDetail() {
        return confirmMoveAllDetail;
    }

    public void setConfirmMoveAllDetail(String confirmMoveAllDetail) {
        this.confirmMoveAllDetail = confirmMoveAllDetail;
    }
}
