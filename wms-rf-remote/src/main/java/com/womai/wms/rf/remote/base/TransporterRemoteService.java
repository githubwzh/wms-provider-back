package com.womai.wms.rf.remote.base;

import com.womai.zlwms.rfsoa.domain.CredentialsVO;
import com.womai.zlwms.rfsoa.domain.base.BaseTransporter;

/**
 * 配送商接口
 * User: zhangwei
 * Date: 2016-05-09
 * To change this template use File | Settings | File Templates.
 */
public interface TransporterRemoteService {


    /**
     * 按照配送商编码查询
     * @param credentialsVO
     * @param transCode 配送商编码
     * @return 查询到的数据
     */
    BaseTransporter getBaseTransporterByTransCode(CredentialsVO credentialsVO,String transCode);
}
