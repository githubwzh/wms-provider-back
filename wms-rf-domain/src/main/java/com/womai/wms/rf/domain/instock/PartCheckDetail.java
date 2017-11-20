package com.womai.wms.rf.domain.instock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * 单一质检业务domain
 * Created by wangzhanhua on 2016/6/17.
 */
public class PartCheckDetail {
    @Receiver(colTip = "ASN单号/网络订单号:",cursorDown = true,topTip = true)
    private String orderCode;
    @Receiver(colTip = "选择ASN单号:")
    private String selectAsn;
    @Receiver(colTip = "商品条码:",cursorDown = true,topTip = true)
    private String barcode;
    @Receiver(colTip = "选择序号:",topTip = true)
    private String serialno;//选择质检明细的序号
    @Receiver(colTip = "质检数量BU:",topTip = true)
    private String checkBu;
    @Receiver(colTip = "质检结果:",topTip = true)
    private String checkStatus;
    @Receiver(colTip = "选择序号:",topTip = true)
    private String reasonid;//选择原因内容的序号
    @Receiver(colTip = "确认质检(Y/N):",topTip = true)
    private String flagYN;

    public String getSerialno() {
        return serialno;
    }

    public void setSerialno(String serialno) {
        this.serialno = serialno;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSelectAsn() {
        return selectAsn;
    }

    public void setSelectAsn(String selectAsn) {
        this.selectAsn = selectAsn;
    }

    public String getCheckBu() {
        return checkBu;
    }

    public void setCheckBu(String checkBu) {
        this.checkBu = checkBu;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getFlagYN() {
        return flagYN;
    }

    public void setFlagYN(String flagYN) {
        this.flagYN = flagYN;
    }

    public String getReasonid() {
        return reasonid;
    }

    public void setReasonid(String reasonid) {
        this.reasonid = reasonid;
    }

    @Override
    public String toString() {
        return "CheckDetail{" +
                "orderCode='" + orderCode + '\'' +
                ", checkStatus='" + checkStatus + '\'' +
                ", reasonid='" + reasonid + '\'' +
                ", flagYN='" + flagYN + '\'' +
                '}';
    }
}
