package com.womai.wms.rf.domain.outstock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * Created by keke on 17-8-9.
 */
public class OutstockCanclePackOrder {
    @Receiver(colTip = "面单号:", topTip = true)
    private String packcode;
    @Receiver(colTip = "是否确认操作退拣，输入Y/N:", topTip = true)
    private String isContinue;

    public String getIsContinue() {
        return isContinue;
    }

    public void setIsContinue(String isContinue) {
        this.isContinue = isContinue;
    }

    public String getPackcode() {
        return packcode;
    }

    public void setPackcode(String packcode) {
        this.packcode = packcode;
    }
}
