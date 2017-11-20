package com.womai.wms.rf.remote.instock.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.instock.InstockCheckRemoteService;
import com.womai.zlwms.rfsoa.api.service.instock.InstockCheckDetailService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.instock.Instock;
import com.womai.zlwms.rfsoa.domain.instock.InstockCheckDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**质检
 * Created by wangzhanhua on 2016/7/5.
 */
@Service("instockCheckRemoteService")
public class InstockCheckRemoteServiceImpl implements InstockCheckRemoteService{
    @Autowired
    private InstockCheckDetailService instockCheckDetailService;
    /**
     * 整单质检
     *
     * @param credentialsVO
     * @param instock       入库主表
     * @param instockCheckDetail 质检明细要更新的参数
     * @return 提示语
     */
    @Override
    public RemoteResult<String> processCheckDetails(CredentialsVO credentialsVO, Instock instock, InstockCheckDetail instockCheckDetail) {
       return  instockCheckDetailService.processCheckDetails(credentialsVO,instock,instockCheckDetail);
    }

    /**
     * 根据入库单号和商品条码查询质检明细
     *
     * @param credentialsVO
     * @param map           asn单号和商品条码
     * @return
     */
    @Override
    public List<InstockCheckDetail> queryInstockCheckDetails(CredentialsVO credentialsVO, Map<String, Object> map) {
        RemoteResult<List<InstockCheckDetail>> listRemoteResult= instockCheckDetailService.queryInstockCheckDetails(credentialsVO, map);
        if(listRemoteResult.isSuccess()){
            return listRemoteResult.getT();
        }else{
            return null;
        }
    }

    /**
     * 分页展示质检明细
     *
     * @param credentialsVO
     * @param map           查询条件入库单号，状态为打开，及分页的基本条件，当前页码，页最大显示条数
     * @return
     */
    @Override
    public RemoteResult<PageModel<InstockCheckDetail>> queryInstockCheckDetailPageList(CredentialsVO credentialsVO, HashMap<String, Object> map) {
        return instockCheckDetailService.queryInstockCheckDetailPageList(credentialsVO,map);
    }

    /**
     * 单一质检明细确认
     *
     * @param credentialsVO
     * @param instock            主表数据
     * @param instockCheckDetail 质检明细表
     * @return
     */
    @Override
    public RemoteResult<Instock> processPartCheckDetail(CredentialsVO credentialsVO, Instock instock, InstockCheckDetail instockCheckDetail) {
        return instockCheckDetailService.processPartCheckDetail(credentialsVO,instock,instockCheckDetail);
    }
}
