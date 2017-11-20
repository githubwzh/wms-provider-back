package com.womai.wms.rf.domain.outstock;

import com.womai.wms.rf.common.annotation.Receiver;

/**
 * @author wangzhanhua
 * @version 1.0
 * @since 16-5-26 下午4:39
 */
public class ConfirmTransfer {
    @Receiver(colTip = "交接单号：",topTip = true)
    private String transferCode;
    @Receiver(colTip = "请输入：",topTip = true)
    private String confirmFlag;

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public String getConfirmFlag() {
        return confirmFlag;
    }

    public void setConfirmFlag(String confirmFlag) {
        this.confirmFlag = confirmFlag;
    }

    @Override
    public String toString() {
        return "ConfirmTransfer{" +
                "transferCode='" + transferCode + '\'' +
                ", confirmFlag='" + confirmFlag + '\'' +
                '}';
    }
}
