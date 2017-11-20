package com.womai.wms.rf.domain.inventory;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.domain.BaseDomain;

/**
 * ClassDescribe:RF盘点数据接收domain，第一步只接收库位编码
 * Author :zhangwei
 * Date: 2016-11-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public class Inventory extends BaseDomain {

    @Receiver(colTip = TipConstants.WH_CODE, topTip = true)
    private String whCode;//库位编码

    public String getWhCode() {
        return whCode;
    }

    public void setWhCode(String whCode) {
        this.whCode = whCode;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "whCode='" + whCode + '\'' +
                '}';
    }
}
