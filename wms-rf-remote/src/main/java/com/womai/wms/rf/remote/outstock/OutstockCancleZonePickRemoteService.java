package com.womai.wms.rf.remote.outstock;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;

import java.util.List;
import java.util.Map;

/**
 * Created by keke on 17-5-8.
 */
public interface OutstockCancleZonePickRemoteService {
    /**
     * 获取可以取消的拣货任务列表，以及取消原因列表;有未完成的周转箱则提示周转箱号
     *
     * @param credentialsVO
     * @return
     */
    public RemoteResult<Map<String, Object>> getCanaleZonePickList(CredentialsVO credentialsVO);

    /**
     * 取消拣货任务
     *
     * @param credentialsVO
     * @param zoneworksheetnoList
     * @param reasoncontent
     * @return
     */
    public RemoteResult<Map<String, Object>> canaleZonePick(CredentialsVO credentialsVO,List<String> zoneworksheetnoList,String reasoncontent);
}
