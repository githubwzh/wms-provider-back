package com.womai.wms.rf.domain.outstock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * Created by keke on 17-4-25.
 */
public class QuerySendsheetno {
    @Receiver(colTip = "集货库位:", topTip = true)
    private String realstorewhscode;
    @Receiver(colTip = "发货单号:", topTip = true)
    private String sendsheetno;

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
        return "QuerySendsheetno{" +
                "realstorewhscode='" + realstorewhscode + '\'' +
                ", sendsheetno='" + sendsheetno + '\'' +
                '}';
    }
}
