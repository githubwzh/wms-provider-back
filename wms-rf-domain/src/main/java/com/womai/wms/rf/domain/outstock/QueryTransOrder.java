package com.womai.wms.rf.domain.outstock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * @author wangzhanhua
 * @version 1.0
 * @since 16-5-26 下午3:40
 */
public class QueryTransOrder {
    @Receiver(colTip = "面单号：")
    private String packCode;

    public String getPackCode() {
        return packCode;
    }

    public void setPackCode(String packCode) {
        this.packCode = packCode;
    }

    @Override
    public String toString() {
        return "QueryTransOrder{" +
                "packCode='" + packCode + '\'' +
                '}';
    }
}
