package com.womai.wms.rf.domain.stock;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.domain.BaseDomain;

/**
 * ClassDescribe:补货移入数据接收对象
 * Author :zhangwei
 * Date: 2016-10-08
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public class ReplenishMoveIn extends BaseDomain {

    @Receiver(colTip = "补货单号：", cursorDown = true, topTip = true)
    private String shelfCode;// 补货单号
    @Receiver(colTip = "商品条码：", cursorDown = true, topTip = true)
    private String barCode; // 商品条码
    @Receiver(colTip = "选择序号:", topTip = true)
    private String selectSerialNo; // 选择明细列表序号
    @Receiver(colTip = "移入库位：", cursorDown = true, topTip = true)
    private String realityWHCode;//移入库位编码
    @Receiver(colTip = "移入数量BU：", topTip = true)
    private String moveInBu;// 移入数量bu

    public String getShelfCode() {
        return shelfCode;
    }

    public void setShelfCode(String shelfCode) {
        this.shelfCode = shelfCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getSelectSerialNo() {
        return selectSerialNo;
    }

    public void setSelectSerialNo(String selectSerialNo) {
        this.selectSerialNo = selectSerialNo;
    }

    public String getRealityWHCode() {
        return realityWHCode;
    }

    public void setRealityWHCode(String realityWHCode) {
        this.realityWHCode = realityWHCode;
    }

    public String getMoveInBu() {
        return moveInBu;
    }

    public void setMoveInBu(String moveInBu) {
        this.moveInBu = moveInBu;
    }

    @Override
    public String toString() {
        return "ReplenishMoveIn{" +
                "shelfCode='" + shelfCode + '\'' +
                ", barCode='" + barCode + '\'' +
                ", selectSerialNo='" + selectSerialNo + '\'' +
                ", realityWHCode='" + realityWHCode + '\'' +
                ", moveInBu='" + moveInBu + '\'' +
                '}';
    }
}
