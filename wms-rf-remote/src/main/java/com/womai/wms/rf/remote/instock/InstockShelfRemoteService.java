package com.womai.wms.rf.remote.instock;

import com.womai.common.framework.domain.PageModel;
import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.instock.InstockShelforder;
import com.womai.zlwms.rfsoa.domain.instock.InstockShelforderDetail;

import java.util.HashMap;
import java.util.List;

/**
 * ClassDescribe:上架主单远程调用接口
 * Author :zhangwei
 * Date: 2016-08-17
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public interface InstockShelfRemoteService {

    /**
     * 上架主单分页查询
     *
     * @param credentialsVO 通用参数对象
     * @param queryMap      查询参数包含InstockShelforder对象及分页所需数据
     * @return 分页数据
     */
    PageModel<InstockShelforder> queryShelfOrderPageModel(CredentialsVO credentialsVO, HashMap<String, Object> queryMap);

    /**
     * 查询上架主单对应的用户可操作上架明细
     *
     * @param credentialsVO     通用参数对象
     * @param instockShelforder 上架主单对象
     * @return 用户当前可操作明细
     */
    List<InstockShelforderDetail> queryShelfDetailList(CredentialsVO credentialsVO, InstockShelforder instockShelforder);

    /**
     * 分页查询上架明细数据
     *
     * @param credentialsVO 通用参数对象
     * @param queryMap      查询条件包含InstockShelforder对象、InstockShelforderDetail对象及分页所需数据
     * @return 分页数据
     */
    PageModel<InstockShelforderDetail> queryShelfDetailPageModel(CredentialsVO credentialsVO, HashMap<String, Object> queryMap);

    /**
     * 按照托盘编码及上架主单ID查询上架明细
     *
     * @param credentialsVO 通用参数对象
     * @param palletCode    托盘码
     * @param shelfId       上架主单ID
     * @return 一条上架明细数据
     */
    RemoteResult<InstockShelforderDetail> getShelfDetailByPalletCode(CredentialsVO credentialsVO, String palletCode, Long shelfId);


    /**
     * 校验上架扫描的库位是否符合基本规则
     *
     * @param credentialsVO           通用参数对象
     * @param wareHouseNo             仓库编码
     * @param instockShelforderDetail 选择的待上架明细
     * @param isPalletShelf           是否托盘码上架方式
     * @return 符合规则返回true，否则返回false
     */
    boolean checkWareHouseForShelf(CredentialsVO credentialsVO, String wareHouseNo, InstockShelforderDetail instockShelforderDetail, boolean isPalletShelf);

    /**
     * 查询明细的总实际上架数量
     *
     * @param credentialsVO           通用参数对象
     * @param instockShelforderDetail 前端选择的初始明细数据
     * @return 返回总的实际上架数量，考虑拆分明细的情况
     */
    int getShelfActualYnBU(CredentialsVO credentialsVO, InstockShelforderDetail instockShelforderDetail);

    /**
     * 上架确认
     *
     * @param credentialsVO           通用参数对象
     * @param instockShelforderDetail 前端选择的初始上架明细数据
     * @param whCode                  上架库位
     * @param shelfNum                上架数量，如果是托盘码上架则为0
     * @param isPalletShelf           是否托盘码上架方式
     * @return -1:系统异常；0：主单整体上架完成；1：托盘码方式上架；2：商品条码方式上架且对应批次的明细一次性全部上架；3：商品条码上架，且对应批次的明细未全部上架完成
     */
    RemoteResult<Integer> confirmShelf(CredentialsVO credentialsVO, InstockShelforderDetail instockShelforderDetail, String whCode, Integer shelfNum, boolean isPalletShelf);

    /**
     * 查询开关值，判断是否需要确认日期
     * @return
     */
    RemoteResult<String> validateNeedConfirmDate(CredentialsVO credentialsVO);

    /**
     * 校验库位是否合法，包括校验生产日期是否混放，是否超最大sku
     * @param credentialsVO
     * @param scanWHCode 实际库位
     * @param selectedShelfDetail 明细
     * @param isPalletShelf 是否托盘
     * @return
     */
    RemoteResult<Boolean> validateWareHouseForShelf(CredentialsVO credentialsVO, String scanWHCode, InstockShelforderDetail selectedShelfDetail, boolean isPalletShelf);

    /**
     * 校验单号，ASN单号，或者网络订单号
     * @param credentialsVO
     * @param orderCode
     * @return
     */
    RemoteResult<Boolean> validateInstockShelforder(CredentialsVO credentialsVO, String orderCode);

    /**
     * 校验商品条码是否存在于基础信息表，是否失效，是否存在该订单
     * 注意，该方法放到分页查询，没有符合条件的明细汇总信息之后，用来提示具体的错误信息
     * @param credentialsVO
     * @param shelfid 上架单id
     * @param barCode 条码
     * @return
     */
    RemoteResult<Boolean> validateBaseGoodsShelfOrder(CredentialsVO credentialsVO , Long shelfid, String barCode);

    /**
     * 判断是否扫描上架单号的开关
     * @param credentialsVO
     * @return
     */
    RemoteResult<String> validateAutoShowShelfCode(CredentialsVO credentialsVO);

    /**
     * 从RF日志中获得，其中一条上架明细的操作人
     * @param credentialsVO
     * @param orderCode
     * @return
     */
    RemoteResult<String> queryShelferNameFromRFLog(CredentialsVO credentialsVO, String orderCode);
}
