package com.womai.wms.rf.domain.instock;

import com.womai.wms.rf.common.annotation.Receiver;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.domain.BaseDomain;

/**
 * 描述: 意向单过账业务Domain
 * User:Qi Xiafei
 * Date: 2016-06-30
 * To change this template use File | Settings | File Templates.
 */
public class IntentionPosting extends BaseDomain{

    @Receiver(colTip = "单据号/网上订单号:",cursorDown = true,topTip = true)
    private String intentionPostingKey;// 意向单过账主键
    @Receiver(colTip="请选择ASN单号:",topTip = true)
    private String asnInstockCode;// ASN单号
    @Receiver(colTip="是否过账(Y/N):",topTip = true)
    private String isPosting;// 是否过账

    @Override
    public String toString() {
        return "IntentionPosting{" +
                "intentionPostingKey='" + intentionPostingKey + '\'' +
                ", asnInstockId='" + asnInstockCode + '\'' +
                ", isPosting='" + isPosting + '\'' +
                '}';
    }

    public String getAsnInstockCode() {
        return asnInstockCode;
    }

    public void setAsnInstockCode(String asnInstockCode) {
        this.asnInstockCode = asnInstockCode;
    }

    public String getIsPosting() {
        return isPosting;
    }

    public void setIsPosting(String isPosting) {
        this.isPosting = isPosting;
    }

    public String getIntentionPostingKey() {
        return intentionPostingKey;
    }

    public void setIntentionPostingKey(String intentionPostingKey) {
        this.intentionPostingKey = intentionPostingKey;
    }
}
