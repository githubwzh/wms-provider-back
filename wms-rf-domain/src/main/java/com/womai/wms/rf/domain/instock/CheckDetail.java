package com.womai.wms.rf.domain.instock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * 整单质检业务domain
 * Created by wangzhanhua on 2016/6/17.
 */
public class CheckDetail {
    @Receiver(colTip = "ASN单号/网络订单号:",cursorDown = true,topTip = true)
    private String orderCode;
    @Receiver(colTip = "选择ASN单号:",topTip = true)
    private String selectAsn;
    @Receiver(colTip = "质检结果:",topTip = true)
    private String checkStatus;
    @Receiver(colTip = "选择序号:",topTip = true)
    private String reasonid;
    @Receiver(colTip = "确认质检(Y/N):",topTip = true)
    private String flagYN;

    public String getSelectAsn() {
        return selectAsn;
    }

    public void setSelectAsn(String selectAsn) {
        this.selectAsn = selectAsn;
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
