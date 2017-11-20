package com.womai.wms.rf.remote.workproduct;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.workproduct.WorkProductShelf;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassDescribe: 加工上架远程调用接口
 * Author :zhangwei
 * Date: 2017-03-07
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public interface ProductShelfRemoteService {

    /**
     * 按照加工单号查询主单数据，并根据人员校验是否存在可操作明细数据
     *
     * @param credentialsVO 基本数据对象
     * @param productNO     加工单号
     * @return 如果存在可操作数据则返回加工单主单数据
     */
    RemoteResult<Map<String, Object>> getProductInfoByNO(CredentialsVO credentialsVO, String productNO);

    /**
     * 按照托盘编码查询一条加工上架明细数据
     *
     * @param credentialsVO    基本数据对象
     * @param workProductShelf 查询条件，包含加工主单id、托盘编码
     * @return 查询到的托盘码对应的上架数据
     */
    RemoteResult<WorkProductShelf> getProductShelfByPalletCode(CredentialsVO credentialsVO, WorkProductShelf workProductShelf);

    /**
     * 分页查询上架明细数据
     *
     * @param credentialsVO 通用参数对象
     * @param queryMap      查询条件包含WorkProductShelf对象及分页所需数据
     * @return 分页数据
     */
    PageModel<WorkProductShelf> queryShelfDetailPageModel(CredentialsVO credentialsVO, HashMap<String, Object> queryMap);


    /**
     * 按照上架主单id及明细行号查询已上架数量BU
     *
     * @param credentialsVO    基本数据对象
     * @param workProductShelf 加工上架明细数据
     * @return
     */
    int getShelfActualYnBU(CredentialsVO credentialsVO, WorkProductShelf workProductShelf);


    /**
     * 上架确认
     *
     * @param credentialsVO    通用参数对象
     * @param workProductShelf 前端选择的初始上架明细数据
     * @param whCode           上架库位
     * @param shelfNum         上架数量，如果是托盘码上架则为0
     * @param isPalletShelf    是否托盘码上架方式
     * @return -1:系统异常；0：主单整体上架完成；1：托盘码方式上架；2：商品条码方式上架且对应批次的明细一次性全部上架；3：商品条码上架，且对应批次的明细未全部上架完成
     */
    RemoteResult<Integer> confirmShelf(CredentialsVO credentialsVO, WorkProductShelf workProductShelf, String whCode, Integer shelfNum, boolean isPalletShelf);


}
