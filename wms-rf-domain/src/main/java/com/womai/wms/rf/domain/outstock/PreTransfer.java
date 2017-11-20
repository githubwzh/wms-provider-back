package com.womai.wms.rf.domain.outstock;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.domain.BaseDomain;

/**
 * User:zhangwei
 * Date: 2016-05-26
 * To change this template use File | Settings | File Templates.
 */
public class PreTransfer extends BaseDomain{
    @Receiver(colTip = "配送商号：",topTip = true)
    private String transCode;
    @Receiver(colTip = "面单号：",topTip = true)
    private String packCode;
    @Receiver(colTip = "",topTip = true)
    private String selectPage;//明细翻页


    public String getSelectPage() {
        return selectPage;
    }

    public void setSelectPage(String selectPage) {
        this.selectPage = selectPage;
    }

    @Override
    public String toString() {
        return "PreTransfer{" +
                "transCode='" + transCode + '\'' +
                ", packCode='" + packCode + '\'' +
                ", selectPage='" + selectPage + '\'' +
                '}';
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getPackCode() {
        return packCode;
    }

    public void setPackCode(String packCode) {
        this.packCode = packCode;
    }

}
