package com.womai.wms.rf.remote.outstock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockPackInfo;
import com.womai.zlwms.rfsoa.domain.outstock.OutstockTransorder;

import java.util.HashMap;
import java.util.Map;

/**
 * 交接单接口
 * User: zhangwei
 * Date: 2016-05-09
 * To change this template use File | Settings | File Templates.
 */
public interface TransOrderRemoteService {



    /**
     * 校验配送商号，查当前用户交接中的交接单及已扫描箱数/应扫描总箱数
     *
     * @param credentialsVO 基本数据对象
     * @param transCode     配送商号
     * @return
     */
    RemoteResult<Map<String, Object>> validateTransporterAndQueryOutStockTransOrder(CredentialsVO credentialsVO, String transCode);


    /**
     * 确认出库
     *
     * @param credentialsVO
     * @param outstockTransorder
     * @return
     */
    RemoteResult<String> confirmOutStock(CredentialsVO credentialsVO, OutstockTransorder outstockTransorder);


    /**
     * 扫描箱号进行交接
     *
     * @param credentialsVO
     * @param packCode      箱号
     * @param transCode     配送商号
     * @param transOrderID    交接单号
     * @return 不为空则添加失败
     */
    Map<String, Object> transOrderByPackCode(CredentialsVO credentialsVO, String packCode, String transCode,Long transOrderID);

    /**
     * 确认交接完成
     * @param credentialsVO
     * @param transCode 配送商编号
     * @param transSheetId 交接单号
     * @return 为空则完成成功，不为空则为失败原因或未交接完成的出库单号
     */
    RemoteResult<String> transOrderFinishScan(CredentialsVO credentialsVO,String transCode,Long transSheetId);

    /**
     *
     *
     * @param credentialsVO
     * @param packCode      箱号
     * @return 不为空则添加失败
     */
    RemoteResult<Map<String, Object>> QueryTransOrderByPackCode(CredentialsVO credentialsVO, String packCode);

    /**
     * 确认交接完成失败时分页提示箱号
     * @param credentialsVO
     * @param  hashMap 分页条件
     * @return 为空则完成成功，不为空则为失败原因或未交接完成的箱号
     */
    RemoteResult<PageModel<OutstockPackInfo>> getOutstockPackInfoPageList(CredentialsVO credentialsVO,HashMap<String,Object> hashMap);
}
