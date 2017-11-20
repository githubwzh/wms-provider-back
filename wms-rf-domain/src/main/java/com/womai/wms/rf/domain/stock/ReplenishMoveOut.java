package com.womai.wms.rf.domain.stock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * ClassDescribe: RF补货移出业务对象
 * Author :Xiafei Qi
 * Date: 2016-09-28
 * Since
 * To change this template use File | Settings | File Templates.
 */
public class ReplenishMoveOut {

    @Receiver(colTip = "补货单号：", topTip = true,cursorDown = true)
    private String shelfcode;// 补货单号

    @Receiver(colTip = "移出库位：", topTip = true,cursorDown = true)
    private String srcWhsCode;// 原库位编码

    @Receiver(colTip = "商品条码：", topTip = true,cursorDown = true)
    private String barCode; // 商品条码

    @Receiver(colTip = "选择序号:", topTip = true)
    private String selectSerialNo; // 选择明细列表序号

    @Receiver(colTip = "移出数量BU：", topTip = true)
    private String moveoutBu;// 移出数量bu
    @Override
    public String toString() {
        return "ReplenishMoveOut{" +
                "shelfcode='" + shelfcode + '\'' +
                ", srcWhsCode='" + srcWhsCode + '\'' +
                ", barCode='" + barCode + '\'' +
                ", selectSerialNo='" + selectSerialNo + '\'' +
                ", moveoutBu='" + moveoutBu + '\'' +
                '}';
    }

    public String getMoveoutBu() {
        return moveoutBu;
    }

    public void setMoveoutBu(String moveoutBu) {
        this.moveoutBu = moveoutBu;
    }

    public String getSelectSerialNo() {
        return selectSerialNo;
    }

    public void setSelectSerialNo(String selectSerialNo) {
        this.selectSerialNo = selectSerialNo;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getSrcWhsCode() {
        return srcWhsCode;
    }

    public void setSrcWhsCode(String srcWhsCode) {
        this.srcWhsCode = srcWhsCode;
    }

    public String getShelfcode() {
        return shelfcode;
    }

    public void setShelfcode(String shelfcode) {
        this.shelfcode = shelfcode;
    }




}
