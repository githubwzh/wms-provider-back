package com.womai.wms.rf.remote.outstock.impl;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.outstock.OutstockCanclePackOrderRemoteService;
import com.womai.zlwms.rfsoa.api.service.outstock.OutstockCanclePackOrderService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockPackInfo;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockProductorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by keke on 17-8-17.
 */
@Service("outstockCanclePackOrderRemoteService")
public class OutstockCanclePackOrderRemoteServiceImpl implements OutstockCanclePackOrderRemoteService {
    @Autowired
    private OutstockCanclePackOrderService outstockCanclePackOrderService;
    /**
     * 校验扫描的面单号是否可以退拣
     *
     * @param credentialsVO
     * @param packcode
     * @return
     */
    @Override
    public RemoteResult<Map<String, Object>> validateCanalePackOrderByPackcode(CredentialsVO credentialsVO, String packcode) {
        return outstockCanclePackOrderService.validateCanalePackOrderByPackcode(credentialsVO, packcode);
    }

    /**
     * 退拣包裹
     *
     * @param credentialsVO
     * @param outstockProductorder
     * @param outstockPackInfo
     * @return
     */
    @Override
    public RemoteResult<Map<String, Object>> confirmCanalePackOrder(CredentialsVO credentialsVO, OutstockProductorder outstockProductorder, OutstockPackInfo outstockPackInfo) {
        return outstockCanclePackOrderService.confirmCanalePackOrder(credentialsVO, outstockProductorder, outstockPackInfo);
    }
}
