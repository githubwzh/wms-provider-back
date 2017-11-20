package com.womai.wms.rf.remote.outstock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockPackInfo;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockProductorder;

import java.util.Map;

/**
 * Created by keke on 17-8-17.
 */
public interface OutstockCanclePackOrderRemoteService {
    /**
     * 校验扫描的面单号是否可以退拣
     *
     * @param credentialsVO,packcode
     * @return
     */
    public RemoteResult<Map<String, Object>> validateCanalePackOrderByPackcode(CredentialsVO credentialsVO, String packcode);

    /**
     * 退拣包裹
     *
     * @param credentialsVO,outstockProductorder,outstockPackInfo
     * @return
     */
    public RemoteResult<Map<String, Object>> confirmCanalePackOrder(CredentialsVO credentialsVO, OutstockProductorder outstockProductorder,OutstockPackInfo outstockPackInfo);
}
