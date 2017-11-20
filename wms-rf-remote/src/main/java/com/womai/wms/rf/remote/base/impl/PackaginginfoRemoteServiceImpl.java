package com.womai.wms.rf.remote.base.impl;

import com.womai.common.framework.domain.RemoteResult;
import com.womai.wms.rf.remote.base.PackaginginfoRemoteService;
import com.womai.zlwms.rfsoa.api.service.base.BasePackaginginfoService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BasePackaginginfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 描述: 商品包装信息接口实现类
 * User:Qi Xiafei
 * Date: 2016-06-22
 * To change this template use File | Settings | File Templates.
 */
@Service("packaginginfoRemoteService")
public class PackaginginfoRemoteServiceImpl implements PackaginginfoRemoteService {

    @Autowired
    BasePackaginginfoService basePackaginginfoService;

    @Override
    public BasePackaginginfo getPackagingInfoById(CredentialsVO credentialsVO, Long pkid) {
        RemoteResult<BasePackaginginfo> remoteResult = basePackaginginfoService.getPackagingInfoById(credentialsVO, pkid);
        if (remoteResult.isSuccess()) {
            return remoteResult.getT();
        }
        return null;
    }

    @Override
    public RemoteResult<BasePackaginginfo> getPackagingInfoByCondition(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo) {
        return basePackaginginfoService.getPackagingInfoByCondition(credentialsVO, basePackaginginfo);
    }

    @Override
    public RemoteResult<Integer> updateLenWidHeiCubageById(CredentialsVO credentialsVO, BasePackaginginfo forUpdate) {
        return basePackaginginfoService.updateLenWidHeiCubageById(credentialsVO, forUpdate);
    }

    @Override
    public RemoteResult<Integer> updateWeightById(CredentialsVO credentialsVO, BasePackaginginfo forUpdate) {
        return basePackaginginfoService.updateWeightById(credentialsVO, forUpdate);
    }

    @Override
    public RemoteResult<Integer> updateIsMtById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo) {
        return basePackaginginfoService.updateIsMtById(credentialsVO, basePackaginginfo);
    }

    @Override
    public RemoteResult<Integer> updateStartyardnmById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo) {
        return basePackaginginfoService.updateStartyardnmById(credentialsVO, basePackaginginfo);
    }

    @Override
    public RemoteResult<Integer> updateTraylevelById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo) {
        return basePackaginginfoService.updateTraylevelById(credentialsVO, basePackaginginfo);
    }

    @Override
    public RemoteResult<Integer> updateOneyardnumById(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfo) {
        return basePackaginginfoService.updateOneyardnumById(credentialsVO, basePackaginginfo);
    }

    @Override
    public RemoteResult<String> insertBasePackaginginfo(CredentialsVO credentialsVO, BasePackaginginfo basePackaginginfoForInsert) {
        return basePackaginginfoService.insertBasePackaginginfo(credentialsVO,basePackaginginfoForInsert);
    }
}
