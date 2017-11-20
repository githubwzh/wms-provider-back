package com.womai.wms.rf.manager.auth.interceptor;

import com.womai.person.api.domain.User;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.SpringContextHolder;
import com.womai.wms.rf.manager.auth.login.BaseShellManagerImpl;
import com.womai.wms.rf.manager.util.UserCache;
import com.womai.wms.rf.manager.util.UserMenuAuthCache;
import io.netty.channel.ChannelHandlerContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import redis.clients.jedis.exceptions.JedisException;

/**
 * aop拦截器，对manager中指定的方法进行拦截
 * User: zhangwei
 * Date: 2016-04-26
 * To change this template use File | Settings | File Templates.
 */
public class UserAndAuthInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object[] args = methodInvocation.getArguments();
        ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) args[0];
        BaseShellManagerImpl baseShellManager = SpringContextHolder.getBean("baseShellManager");
        Long userId = baseShellManager.getCurrentUserId(channelHandlerContext);
        User user = UserCache.getUser(userId);
        boolean userCheckResult = checkUser(user);// 校验用户，如果校验未通过，则返回登录界面
        if (!userCheckResult) {
            HandlerUtil.clearAll(channelHandlerContext.channel());//清屏
            baseShellManager.forward(Constants.LOGIN_SHELL_MANAGER, channelHandlerContext);
            return null;
        }
        UserMenuAuthCache userMenuAuthCache = SpringContextHolder.getBean("userMenuAuthCache");
        // 校验菜单权限，如果校验未通过，则返回主页面
        boolean menuAuthCheckResult = checkMenuAuth(baseShellManager.getCurrentUserId(channelHandlerContext), baseShellManager.getCurrentSite(channelHandlerContext), userMenuAuthCache);
        if (!menuAuthCheckResult) {
            userMenuAuthCache.refreshUserMenuAuthCache(baseShellManager.getCurrentUserId(channelHandlerContext), baseShellManager.getCurrentSite(channelHandlerContext)); // 刷新用户菜单权限缓存
            baseShellManager.toMenuWindow(channelHandlerContext);// 回到主菜单
            return null;
        }
        return methodInvocation.proceed();
    }

    /**
     * 校验用户缓存是否存在
     *
     * @return true:验证通过;false:验证不通过
     */
    private Boolean checkUser(User user) {
        if (user == null) {
            return false;
        } else {
            UserCache.extendUserCacheTime(user.getId());
            return true;
        }
    }

    /**
     * 校验权限是否有变化
     *
     * @return true:验证通过;false:验证不通过
     */
    private boolean checkMenuAuth(Long userId, String currentSite, UserMenuAuthCache userMenuAuthCache) throws Exception {
        // 校验权限
        return !userMenuAuthCache.isChangeUserMenu(userId, currentSite);
    }
}
