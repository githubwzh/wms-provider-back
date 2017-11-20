package com.womai.wms.rf.remote.instock.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.instock.InstockReasonRemoteService;
import com.womai.zlwms.rfsoa.api.service.instock.InstockReasonService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * Created by wangzhanhua on 2016/7/4.
 */
@Service("instockReasonRemoteService ")
public class InstockReasonRemoteServiceImpl implements InstockReasonRemoteService {
    @Autowired
    private InstockReasonService instockReasonService ;

    /**
     * 分页查询原因表
     *
     * @param credentialsVO
     * @param map           必需参数：第几页，一页多少数据。可选参数，排序字段，排序规则，查询数据筛选条件
     * @return 如果有异常，查询结果无数据，或者参数不对返回RemoteResult.success 为false
     */
    @Override
    public RemoteResult<PageModel<InstockReason>> getInstockReasonPageList(CredentialsVO credentialsVO, HashMap<String, Object> map) {
        return instockReasonService.getInstockReasonPageList(credentialsVO,map);
    }

    /**
     * 根据原因类型查询原因
     *
     * @param credentialsVO 登录信息
     * @param type          原因类型
     * @return 原因集合
     */
    @Override
    public RemoteResult<List<InstockReason>> queryInstockReasonsByType(CredentialsVO credentialsVO, List<Integer> type) {
        return instockReasonService.queryInstockReasonsByType(credentialsVO,type);
    }
}
