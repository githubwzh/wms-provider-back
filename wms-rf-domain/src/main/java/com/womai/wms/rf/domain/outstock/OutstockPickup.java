package com.womai.wms.rf.domain.outstock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * Created by keke on 17-4-25.
 */
public class OutstockPickup {
    @Receiver(colTip = "周转箱:", topTip = true)
    private String containerno;
    @Receiver(colTip = "推荐库位:", topTip = true)
    private String storewhscode;
    @Receiver(colTip = "集货库位:", topTip = true)
    private String realstorewhscode;
    @Receiver(colTip = "发货单号:", topTip = true)
    private String sendsheetno;

    public String getContainerno() {
        return containerno;
    }

    public void setContainerno(String containerno) {
        this.containerno = containerno;
    }

    public String getStorewhscode() {
        return storewhscode;
    }

    public void setStorewhscode(String storewhscode) {
        this.storewhscode = storewhscode;
    }

    public String getRealstorewhscode() {
        return realstorewhscode;
    }

    public void setRealstorewhscode(String realstorewhscode) {
        this.realstorewhscode = realstorewhscode;
    }

    public String getSendsheetno() {
        return sendsheetno;
    }

    public void setSendsheetno(String sendsheetno) {
        this.sendsheetno = sendsheetno;
    }


    @Override
    public String toString() {
        return "OutstockPickup{" +
                "containerno='" + containerno + '\'' +
                ", storewhscode='" + storewhscode + '\'' +
                ", realstorewhscode='" + realstorewhscode + '\'' +
                ", sendsheetno='" + sendsheetno + '\'' +
                '}';
    }
}
