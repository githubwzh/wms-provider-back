package com.womai.wms.rf.manager.auth.interceptor;

import com.womai.wms.rf.common.constants.KeyEnum;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * ClassDescribe: 拦截channelRead方法中读取的object参数是否为空字符 KeyEnum.NUT_0对应的值
 * Author :zhangwei
 * Date: 2017-01-04
 * Since:1
 * To change this template use File | Settings | File Templates.
 */
public class NullInputInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object[] args = methodInvocation.getArguments();
        Object userInput = args[1];
        if (KeyEnum.NUT_0.code == userInput.toString().getBytes()[0]) {
            return null;
        }
        return methodInvocation.proceed();
    }
}
