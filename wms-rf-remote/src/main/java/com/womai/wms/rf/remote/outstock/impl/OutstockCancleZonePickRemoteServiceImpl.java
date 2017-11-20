package com.womai.wms.rf.remote.outstock.impl;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.outstock.OutstockCancleZonePickRemoteService;
import com.womai.zlwms.rfsoa.api.service.outstock.OutstockCancleZonePickService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by keke on 17-5-8.
 */
@Service("outstockCancleZonePickRemoteService")
public class OutstockCancleZonePickRemoteServiceImpl implements OutstockCancleZonePickRemoteService {
    @Autowired
    private OutstockCancleZonePickService outstockCancleZonePickService;
    /**
     * 获取可以取消的拣货任务列表，以及取消原因列表;有未完成的周转箱则提示周转箱号
     *
     * @param credentialsVO
     * @return
     */
    @Override
    public RemoteResult<Map<String, Object>> getCanaleZonePickList(CredentialsVO credentialsVO) {
        return outstockCancleZonePickService.getCanaleZonePickList(credentialsVO);
    }

    /**
     * 取消拣货任务
     *
     * @param credentialsVO
     * @param zoneworksheetnoList
     * @param reasoncontent
     * @return
     */
    @Override
    public RemoteResult<Map<String, Object>> canaleZonePick(CredentialsVO credentialsVO, List<String> zoneworksheetnoList, String reasoncontent) {
        return outstockCancleZonePickService.canaleZonePick(credentialsVO, zoneworksheetnoList, reasoncontent);
    }
}
