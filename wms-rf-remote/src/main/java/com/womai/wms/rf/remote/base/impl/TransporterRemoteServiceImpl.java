package com.womai.wms.rf.remote.base.impl;

import com.womai.wms.rf.remote.base.TransporterRemoteService;
import com.womai.zlwms.rfsoa.api.service.base.BaseTransporterService;
import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BaseTransporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 配送商接口实现类
 * User: zhangwei
 * Date: 2016-05-09
 * To change this template use File | Settings | File Templates.
 */
@Service("transporterRemoteService")
public class TransporterRemoteServiceImpl implements TransporterRemoteService {

    @Autowired
    private BaseTransporterService baseTransporterService;


    @Override
    public BaseTransporter getBaseTransporterByTransCode(CredentialsVO credentialsVO, String transCode) {
        return baseTransporterService.getBaseTransporterByTransCode(credentialsVO,transCode).getT();
    }
}
