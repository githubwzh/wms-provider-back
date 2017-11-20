package com.womai.wms.rf.remote.base;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;

/**
 * 描述: 商品包装信息
 * User:Qi Xiafei
 * Date: 2016-06-22
 * To change this template use File | Settings | File Templates.
 */
public interface PackaginginfoRemoteService {

    /**
     * 按照pkid查询包装信息
     *
     * @param credentialsVO 基础数据对象
     * @param pkid          包装id
     * @return 包装数据对象
     */
    BasePackaginginfo getPackagingInfoById(CredentialsVO credentialsVO, Long pkid);


    /**
     * 查询商品包装信息
     *
     * @param credentialsVO
     * @param basePackaginginfo 查询条件 包含skuid、packlevel、packstatus
     * @return 商品包装信息
     */
    RemoteResult<BasePackaginginfo> getPackagingInfoByCondition(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo);

    /**
     * 更新商品包装信息的长宽高体积
     *
     * @param credentialsVO
     * @param basePackaginginfo 包含id、长宽高体积信息
     * @return 执行结果 ==1即执行成功
     */
    RemoteResult<Integer> updateLenWidHeiCubageById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo);

    /**
     * 更新商品包装重量
     *
     * @param credentialsVO
     * @param basePackaginginfo 商品包装信息 包含id、skuid、packlevel、weight
     * @return 执行结果 ==1即执行成功
     */
    RemoteResult<Integer> updateWeightById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo);

    /**
     * 更新商品包装是否码托字段
     *
     * @param credentialsVO
     * @param basePackaginginfo id、ismt
     * @return
     */
    RemoteResult<Integer> updateIsMtById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo);

    /**
     * 更新商品包装起码数量字段
     *
     * @param credentialsVO
     * @param basePackaginginfo id、Startyardnm
     * @return
     */
    RemoteResult<Integer> updateStartyardnmById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo);

    /**
     * 更新商品包装托盘码放层数字段
     *
     * @param credentialsVO
     * @param basePackaginginfo id、Traylevel
     * @return
     */
    RemoteResult<Integer> updateTraylevelById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo);

    /**
     * 更新商品包装单层码托数量字段
     *
     * @param credentialsVO
     * @param basePackaginginfo id、Oneyardnum
     * @return
     */
    RemoteResult<Integer> updateOneyardnumById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo);

    /**
     * 新增包装
     * @param credentialsVO 登录信息
     * @param basePackaginginfoForInsert 包装对象
     * @return 提示语
     */
    RemoteResult<String> insertBasePackaginginfo(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfoForInsert);
}
