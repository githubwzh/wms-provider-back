package com.womai.wms.rf.manager.window.base;

import com.womai.wms.rf.common.constants.TipConstants;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.zlwms.rfsoa.domain.base.BaseGoodsinfo;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * ClassDescribe:商品维护参数类
 * Author :wangzhanhua
 * Date: 2017-03-09
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component(TipConstants.GOODSINFO_PARAM_MANAGER)
public class GoodsInfoParamManagerImpl extends ReceiveManager {
    private BaseGoodsinfo baseGoodsinfo ;//商品信息
    private BasePackaginginfo basePackagingInfoLevel1;//存储数据库中查询到的商品一级包装信息
    public BaseGoodsinfo getBaseGoodsinfo() {
        return baseGoodsinfo;
    }

    public void setBaseGoodsinfo(BaseGoodsinfo baseGoodsinfo) {
        this.baseGoodsinfo = baseGoodsinfo;
    }

    public BasePackaginginfo getBasePackagingInfoLevel1() {
        return basePackagingInfoLevel1;
    }

    public void setBasePackagingInfoLevel1(BasePackaginginfo basePackagingInfoLevel1) {
        this.basePackagingInfoLevel1 = basePackagingInfoLevel1;
    }
}
