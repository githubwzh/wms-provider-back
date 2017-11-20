package com.womai.wms.rf.remote.outstock.impl;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.outstock.TransOrderRemoteService;
import com.womai.zlwms.rfsoa.api.service.outstock.OutStockTransOrderService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockPackInfo;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockTransorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 交接单接口实现类
 * User: zhangwei
 * Date: 2016-05-09
 * To change this template use File | Settings | File Templates.
 */
@Service("transOrderRemoteService")
public class TransOrderRemoteServiceImpl implements TransOrderRemoteService {
    @Autowired
    private OutStockTransOrderService outStockTransOrderService;

    @Override
    public RemoteResult<Map<String, Object>> validateTransporterAndQueryOutStockTransOrder(CredentialsVO credentialsVO, String transCode) {
        return outStockTransOrderService.validateTransporterAndQueryOutStockTransOrder(credentialsVO,transCode);
    }

    @Override
    public RemoteResult<String> confirmOutStock(CredentialsVO credentialsVO, OutstockTransorder outstockTransorder) {
            return outStockTransOrderService.confirmOutStock(credentialsVO, outstockTransorder);
    }

    @Override
    public Map<String, Object> transOrderByPackCode(CredentialsVO credentialsVO, String packCode, String transCode,Long transOrderID) {
        RemoteResult<Map<String, Object>> remoteResult = outStockTransOrderService.transOrderByPackCode(credentialsVO,packCode,transCode,transOrderID);
        return remoteResult.getT();
    }

    @Override
    public RemoteResult<String> transOrderFinishScan(CredentialsVO credentialsVO, String transCode,Long transSheetId) {
        return outStockTransOrderService.transOrderFinishScan(credentialsVO, transCode,transSheetId);
    }

    /**
     * @param credentialsVO
     * @param packCode      箱号
     * @return 不为空则添加失败
     */
    @Override
    public RemoteResult<Map<String, Object>> QueryTransOrderByPackCode(CredentialsVO credentialsVO, String packCode) {
        return outStockTransOrderService.getTransOrderByPackCode(credentialsVO,packCode);
    }

    /**
     * 确认交接完成失败时分页提示箱号
     *
     * @param credentialsVO
     * @param hashMap 分页条件
     * @return 为空则完成成功，不为空则为失败原因或未交接完成的箱号
     */
    @Override
    public RemoteResult<PageModel<OutstockPackInfo>> getOutstockPackInfoPageList(CredentialsVO credentialsVO, HashMap<String,Object> hashMap) {
        return outStockTransOrderService.getOutstockPackInfoPageList(credentialsVO,hashMap);
    }
}
