package com.womai.wms.rf.domain.outstock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * Created by keke on 17-5-4.
 */
public class CancleZonePick {
    @Receiver(colTip = "按区拣货单号:", topTip = true)
    private String zoneworksheetno;
    @Receiver(colTip = "选择取消原因:", topTip = true)
    private String reasoncontent;
    @Receiver(colTip = "确认取消请输入Y:", topTip = true)
    private String applyCancle;

    public String getApplyCancle() {
        return applyCancle;
    }

    public void setApplyCancle(String applyCancle) {
        this.applyCancle = applyCancle;
    }

    public String getZoneworksheetno() {
        return zoneworksheetno;
    }

    public void setZoneworksheetno(String zoneworksheetno) {
        this.zoneworksheetno = zoneworksheetno;
    }

    public String getReasoncontent() {
        return reasoncontent;
    }

    public void setReasoncontent(String reasoncontent) {
        this.reasoncontent = reasoncontent;
    }
}
