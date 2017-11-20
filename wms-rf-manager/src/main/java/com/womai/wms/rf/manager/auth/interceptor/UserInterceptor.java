package com.womai.wms.rf.manager.auth.interceptor;

import com.womai.person.api.domain.User;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.SpringContextHolder;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.manager.auth.login.BaseShellManagerImpl;
import com.womai.wms.rf.manager.util.UserCache;
import io.netty.channel.ChannelHandlerContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * aop拦截器，对manager中指定的方法进行拦截
 * User: zhangwei
 * Date: 2016-04-26
 * To change this template use File | Settings | File Templates.
 */
public class UserInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object[] args = methodInvocation.getArguments();
        ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) args[0];
        boolean userLegal = checkUserAndRole();//如果校验未通过，则返回登录界面
        if (!userLegal) {
            HandlerUtil.clearAll(channelHandlerContext.channel());//清屏
            BaseShellManagerImpl baseShellManager = SpringContextHolder.getBean("baseShellManager");
            baseShellManager.forward(Constants.LOGIN_SHELL_MANAGER, channelHandlerContext);
            return null;
        }
        return methodInvocation.proceed();
    }

    /**
     * 目前只验证用户缓存是否存在，后期需要增加权限验证
     *
     * @return true:验证通过;false:验证不通过
     */
    private Boolean checkUserAndRole() {
        boolean result = true;
//        BaseShellManagerImpl baseShellManager = SpringContextHolder.getBean("baseShellManager");
//        Long userId = baseShellManager.getCurrentUserId();
//        User userInCache = UserCache.getUser(userId);
//        WMSDebugManager.debugLog("拦截器userId:"+userId+">>"+userInCache);
//        if (userInCache == null) {
//            result = false;
//        } else {
//            UserCache.extendUserCacheTime(userId);
//        }
        return result;
    }
}
