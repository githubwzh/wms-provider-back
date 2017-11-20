package com.womai.wms.rf.remote.base;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BaseDictionary;
import com.womai.zlwms.rfsoa.domain.base.BaseGoodsinfo;

import java.util.List;

/**
 * 描述:商品信息接口
 * User:Qi Xiafei
 * Date: 2016-06-20
 * To change this template use File | Settings | File Templates.
 */
public interface GoodsinfoRemoteService {
    /**
     * 根据条形码查询商品信息
     *
     * @param credentialsVO 登录用户信息对象
     * @param barcode       条形码
     * @return 返回条形码商品信息对象
     */
    RemoteResult<BaseGoodsinfo> getGoodsInfoByBarCode(CredentialsVO credentialsVO, String barcode);

    /**
     * 获取生效的商品数据
     *
     * @param credentialsVO 登录用户信息对象
     * @param barcode       条形码
     * @return 有效的商品数据
     */
    BaseGoodsinfo getEnableGoodsByBarCode(CredentialsVO credentialsVO, String barcode);

    /**
     * 以SKuId为依据更新一条商品的是否贵品字段
     *
     * @param credentialsVO 登录用户信息对象
     * @param baseGoodsinfo 更新条件，包括商品ID和是否贵品字段 0--否，1--是
     * @return 执行结果，==1代表执行成功
     */
    RemoteResult<Integer> updateColValuableFlagBySkuId(CredentialsVO credentialsVO, BaseGoodsinfo baseGoodsinfo);

    /**
     * 以SKuId为依据更新一条商品的批次规则字段
     *
     * @param credentialsVO 登录用户信息对象
     * @param baseGoodsinfo 更新条件，包括商品ID和批次规则 0--普通批次，1--百货批次
     * @return 执行结果，==1代表执行成功
     */
    RemoteResult<Integer> updateColBatchRuleBySkuId(CredentialsVO credentialsVO, BaseGoodsinfo baseGoodsinfo);


    /**
     * 查询商品上架属性字典列表，remark='SHELFFLAG' orderby id
     *
     * @param credentialsVO 登录用户信息对象
     * @return 商品上架属性字典列表
     */
    List<BaseDictionary> getShelfFlagList(CredentialsVO credentialsVO);

    /**
     * 以SKuId为依据更新一条商品的批次规则字段
     *
     * @param credentialsVO 登录用户信息对象
     * @param forUpdate 更新条件，包括商品ID和商品上架属性
     * @return 执行结果，isSuccess()==true 执行成功
     */
    RemoteResult<Integer> updateShelfFlagBySkuId(CredentialsVO credentialsVO, BaseGoodsinfo forUpdate);
}
