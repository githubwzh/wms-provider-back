package com.womai.wms.rf.remote.instock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.instock.InstockReason;

import java.util.HashMap;
import java.util.List;

/**原因管理接口
 * Created by wangzhanhua on 2016/7/4.
 */
public interface InstockReasonRemoteService {
    /**
     * 分页查询原因表
     * @param credentialsVO
     * @param map 必需参数：第几页，一页多少数据。可选参数，排序字段，排序规则，查询数据筛选条件
     * @return 如果有异常,查询结果无数据，或者参数不对返回RemoteResult.success 为false
     */
    RemoteResult<PageModel<InstockReason>> getInstockReasonPageList(CredentialsVO credentialsVO,HashMap<String,Object> map);

    /**
     * 根据原因类型查询原因
     * @param credentialsVO 登录信息
     * @param type 原因类型
     * @return 原因集合
     */
    RemoteResult<List<InstockReason>> queryInstockReasonsByType(CredentialsVO credentialsVO,List<Integer> type);
}
