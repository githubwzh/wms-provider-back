package com.womai.wms.rf.manager.window.instock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.common.constants.*;
import com.womai.wms.rf.common.util.*;
import com.womai.wms.rf.domain.instock.QuickInstock;
import com.womai.wms.rf.domain.instock.ShelfOrder;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.remote.instock.InstockReasonRemoteService;
import com.womai.wms.rf.remote.instock.InstockRemoteService;
import com.womai.zlwms.rfsoa.api.WmsConstants;
import com.womai.zlwms.rfsoa.domain.base.BasePallet;
import com.womai.zlwms.rfsoa.domain.instock.Instock;
import com.womai.zlwms.rfsoa.domain.instock.InstockDetail;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by keke on 17-8-28.
 */
@Scope("prototype")
@Component("quickInstockParamManager")
public class QuickInstockParamManagerImpl extends ReceiveManager {
    private ShelfOrder shelfOrder;

    public ShelfOrder getShelfOrder() {
        return shelfOrder;
    }

    public void setShelfOrder(ShelfOrder shelfOrder) {
        this.shelfOrder = shelfOrder;
    }
}
